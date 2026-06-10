const { app, BrowserWindow, ipcMain, dialog, shell, Menu } = require('electron')
const path = require('path')
const fs = require('fs')

//start backend in backround
const { spawn } = require('child_process')
let backendProcess = null

function startBackend() {
  const jarPath = app.isPackaged
    ? path.join(process.resourcesPath, 'backend', 'backend.jar')
    : path.join(__dirname, 'backend', 'backend.jar')

  const javaExecutable = app.isPackaged
    ? path.join(process.resourcesPath, 'runtime', 'bin', 'java.exe')
    : 'java'

  const logPath = path.join(app.getPath('userData'), 'backend.log')
  const dbPath = path.join(app.getPath('userData'), 'billory.db')
  const dbUrl = `jdbc:sqlite:${dbPath.replace(/\\/g, '/')}`

  fs.appendFileSync(
    logPath,
    `\n--- Backend start ---\njavaExecutable=${javaExecutable}\njarPath=${jarPath}\nresourcesPath=${process.resourcesPath}\ndbUrl=${dbUrl}\n`,
    'utf8'
  )

  backendProcess = spawn(javaExecutable, ['-jar', jarPath], {
    detached: false,
    stdio: 'pipe',
    env: {
      ...process.env,
      BILLORY_DB_URL: dbUrl,
    },
  })

  backendProcess.stdout.on('data', (data) => {
    fs.appendFileSync(logPath, `[OUT] ${data.toString()}`, 'utf8')
  })

  backendProcess.stderr.on('data', (data) => {
    fs.appendFileSync(logPath, `[ERR] ${data.toString()}`, 'utf8')
  })

  backendProcess.on('exit', (code) => {
    fs.appendFileSync(logPath, `\n[EXIT] Backend exited with code ${code}\n`, 'utf8')
  })

  backendProcess.on('error', (error) => {
    console.error('Backend konnte nicht gestartet werden:', error)
  })
}

function waitForBackend(maxAttempts = 30) {
    return new Promise((resolve, reject) => {
      let attempts = 0

      const checkBackend = async () => {
        attempts++

        try {
          const response = await fetch('http://localhost:8080/api/settings')

          if (response.status === 200 || response.status === 404) {
            resolve()
            return
          }
        } catch {
          // Backend noch nicht bereit
        }

        if (attempts >= maxAttempts) {
          reject(new Error('Backend wurde nicht rechtzeitig gestartet.'))
          return
        }

        setTimeout(checkBackend, 500)
      }

      checkBackend()
    })
  }

  ipcMain.handle('dialog:select-pdf-file', async () => {
    const result = await dialog.showOpenDialog({
      title: 'PDF-Datei auswählen',
      filters: [
        { name: 'PDF-Dateien', extensions: ['pdf'] }
      ],
      properties: ['openFile']
    })

    if (result.canceled || result.filePaths.length === 0) {
      return null
    }

    return result.filePaths[0]
  })

  ipcMain.handle('shell:open-file', async (_event, filePath) => {
    if (!filePath) {
      return false
    }

    await shell.openPath(filePath)
    return true
  })

ipcMain.handle('shell:open-folder', async (_event, fileOrFolderPath) => {
    if (!fileOrFolderPath) {
      return false
    }

    const folderPath = path.extname(fileOrFolderPath)
      ? path.dirname(fileOrFolderPath)
      : fileOrFolderPath

    await shell.openPath(folderPath)
    return true
  })

ipcMain.handle('billory:open-standard-folder', async (_event, folderName) => {
  const basePath = path.join(
    app.getPath('documents'),
    'Billory'
  )

  const allowedFolders = [
    'Rechnungen',
    'Angebote',
    'Mahnungen',
    'Belege',
    'Backups',
  ]

  if (!allowedFolders.includes(folderName)) {
    return false
  }

  const targetPath = path.join(basePath, folderName)

  if (!fs.existsSync(targetPath)) {
    fs.mkdirSync(targetPath, { recursive: true })
  }

  await shell.openPath(targetPath)

  return true
})

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js')
    }
  })

  if (app.isPackaged) {
    win.loadFile(path.join(__dirname, '../../dist/renderer/index.html'))
  } else {
    win.loadURL('http://localhost:5173')
  }
}

app.whenReady().then(async () => {
  startBackend()

  Menu.setApplicationMenu(null)

  try {
    await waitForBackend()
    createWindow()
  } catch (error) {
    console.error('Backend-Start fehlgeschlagen:', error)
    createWindow()
  }
})

app.on('before-quit', () => {
  if (backendProcess) {
    backendProcess.kill()
    backendProcess = null
  }
})
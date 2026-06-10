const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
  selectPdfFile: () => ipcRenderer.invoke('dialog:select-pdf-file'),
  openFile: (filePath) => ipcRenderer.invoke('shell:open-file', filePath),
  openFolder: (folderPath) => ipcRenderer.invoke('shell:open-folder', folderPath)
})
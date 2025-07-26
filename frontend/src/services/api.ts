import axios from 'axios'

const client = axios.create({
  baseURL: '/api'
})

export const api = {
  async listFiles(directory: string): Promise<string[]> {
    const { data } = await client.get(`/files/${directory}`)
    return data
  },

  async getFile(directory: string, filename: string): Promise<string> {
    const { data } = await client.get(`/files/${directory}/${filename}`)
    return data
  },

  async updateFile(directory: string, filename: string, content: string): Promise<void> {
    await client.put(`/files/${directory}/${filename}`, content)
  },

  async deleteFile(directory: string, filename: string): Promise<void> {
    await client.delete(`/files/${directory}/${filename}`)
  },

  async createFile(directory: string, filename: string): Promise<void> {
    await client.put(`/files/${directory}/${filename}`, '')
  },

  downloadFile(directory: string, filename: string): void {
    window.open(`/api/files/${directory}/${filename}`, '_blank')
  },

  async runBenchmark(config: string, tasks: string[], resultFile: string): Promise<string> {
    const { data } = await client.post('/benchmark/run', { config, tasks, resultFile })
    return data.id
  },

  async getBenchmarkStatus(id: string): Promise<any> {
    const { data } = await client.get(`/benchmark/status/${id}`)
    return data
  }
}

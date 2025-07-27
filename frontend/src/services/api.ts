import axios from 'axios'
import { parse } from 'yaml'

const client = axios.create({
  baseURL: '/api',
  headers: {
//     'Content-Type': 'text/plain'  // Important for sending raw string content
  }
})

interface StartBenchmarkParams {
  execConfig: string;
  taskSources: string[];
  resultFilename: string;
}

export const api = {
  async listFiles(directory: string): Promise<string[]> {
    try {
      const response = await client.get(`/files/${directory}`)
      console.log('API response:', response)
      return Array.isArray(response.data) ? response.data : []
    } catch (error) {
      console.error('API error:', error)
      return []
    }
  },

  async getFile(directory: string, filename: string): Promise<string> {
    const { data } = await client.get(`/files/${directory}/${filename}`)
    return data
  },

  async updateFile(directory: string, filename: string, content: string): Promise<void> {
    await client.put(`/files/${directory}/${filename}`, content, {
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  },

  async deleteFile(directory: string, filename: string): Promise<void> {
    await client.delete(`/files/${directory}/${filename}`)
  },

  async createFile(directory: string, filename: string): Promise<void> {
    await client.put(`/files/${directory}/${filename}`, ' ', {
      headers: {
        'Content-Type': 'text/plain'
      },
      transformRequest: [(data) => data]  // Prevent axios from JSON stringifying
    })
  },

  downloadFile(directory: string, filename: string): void {
    window.open(`/api/files/${directory}/${filename}`, '_blank')
  },

  async runBenchmark(config: string, tasks: string[], resultFile: string): Promise<string> {
    const { data } = await client.post('/benchmark/run', { config, tasks, resultFile })
    return data.id
  },

  async startBenchmark(params: StartBenchmarkParams): Promise<string> {
    const { data } = await client.post('/benchmark/start', params);
    return data.statusFile;
  },

  async getBenchmarkStatus(statusFile: string): Promise<any> {
    const { data } = await client.get(`/benchmark/status/${statusFile}`);
    return data;
  },

  async parseYaml(content: string): Promise<any> {
    try {
      return parse(content);
    } catch (error) {
      console.error('YAML parsing error:', error);
      throw error;
    }
  },

};

import axios from "axios";

const BASE_URL = "http://localhost:5000";

class ApiService {
  constructor() {
    this.axiosInstance = axios.create({
      baseURL: BASE_URL,
      withCredentials: true,
    });
  }

  // Auth endpoints
  async getCurrentUser() {
    const response = await this.axiosInstance.get("/auth/user");
    return response.data;
  }

  // Vehicle endpoints
  async getVozila() {
    const response = await this.axiosInstance.get("/api/vozila");
    return response.data;
  }

  async addVozilo(data) {
    const response = await this.axiosInstance.post("/api/vozila", data);
    return response.data;
  }

  async deleteVozilo(id) {
    const response = await this.axiosInstance.delete(`/api/vozila/${id}`);
    return response.data;
  }

  // Brand endpoints
  async getMarke() {
    const response = await this.axiosInstance.get("/api/marke");
    return response.data;
  }

  // Model endpoints
  async getModeliByMarka(id_marka) {
    const response = await this.axiosInstance.get(`/api/modeli/${id_marka}`);
    return response.data;
  }
}

export default new ApiService();

import { apiClient } from './client';
import {
  CreateTaskRequest,
  PageResponse,
  Task,
  TaskFilterParams,
  UpdateTaskRequest,
  UpdateTaskStatusRequest,
} from '@/types/task.types';

export const tasksApi = {
  getTasks: async (params?: TaskFilterParams): Promise<PageResponse<Task>> => {
    const queryParams: Record<string, string | number> = {};
    if (params?.status && params.status !== 'ALL') {
      queryParams.status = params.status;
    }
    if (params?.search) {
      queryParams.search = params.search;
    }
    if (params?.page !== undefined) {
      queryParams.page = params.page;
    }
    if (params?.size !== undefined) {
      queryParams.size = params.size;
    }
    if (params?.sort) {
      queryParams.sort = params.sort;
    }

    const response = await apiClient.get<PageResponse<Task>>('/tasks', {
      params: queryParams,
    });
    return response.data;
  },

  getTaskById: async (id: number): Promise<Task> => {
    const response = await apiClient.get<Task>(`/tasks/${id}`);
    return response.data;
  },

  createTask: async (data: CreateTaskRequest): Promise<Task> => {
    const response = await apiClient.post<Task>('/tasks', data);
    return response.data;
  },

  updateTask: async (id: number, data: UpdateTaskRequest): Promise<Task> => {
    const response = await apiClient.put<Task>(`/tasks/${id}`, data);
    return response.data;
  },

  updateTaskStatus: async (id: number, data: UpdateTaskStatusRequest): Promise<Task> => {
    const response = await apiClient.patch<Task>(`/tasks/${id}/status`, data);
    return response.data;
  },

  deleteTask: async (id: number): Promise<void> => {
    await apiClient.delete(`/tasks/${id}`);
  },
};

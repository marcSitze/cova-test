import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { tasksApi } from '@/api/tasks.api';
import {
  CreateTaskRequest,
  TaskFilterParams,
  UpdateTaskRequest,
  UpdateTaskStatusRequest,
} from '@/types/task.types';

export const TASKS_QUERY_KEY = 'tasks';

export const useTasks = (params?: TaskFilterParams) => {
  const queryClient = useQueryClient();

  // Query: Liste des tâches
  const tasksQuery = useQuery({
    queryKey: [TASKS_QUERY_KEY, params],
    queryFn: () => tasksApi.getTasks(params),
    staleTime: 1000 * 60, // Cache valide pendant 1 minute
  });

  // Mutation: Création
  const createMutation = useMutation({
    mutationFn: (data: CreateTaskRequest) => tasksApi.createTask(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });

  // Mutation: Mise à jour complète
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateTaskRequest }) =>
      tasksApi.updateTask(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });

  // Mutation: Modification de statut
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateTaskStatusRequest }) =>
      tasksApi.updateTaskStatus(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });

  // Mutation: Suppression
  const deleteMutation = useMutation({
    mutationFn: (id: number) => tasksApi.deleteTask(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });

  return {
    tasksPage: tasksQuery.data,
    tasks: tasksQuery.data?.content || [],
    isLoading: tasksQuery.isLoading,
    isError: tasksQuery.isError,
    error: tasksQuery.error,
    refetch: tasksQuery.refetch,

    createTask: createMutation.mutateAsync,
    isCreating: createMutation.isPending,

    updateTask: updateMutation.mutateAsync,
    isUpdating: updateMutation.isPending,

    updateTaskStatus: updateStatusMutation.mutateAsync,
    isUpdatingStatus: updateStatusMutation.isPending,

    deleteTask: deleteMutation.mutateAsync,
    isDeleting: deleteMutation.isPending,
  };
};

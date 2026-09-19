import React, { useState } from 'react';
import { Navbar } from '@/components/layout/Navbar';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Alert } from '@/components/common/Alert';
import { TaskCard } from '@/components/tasks/TaskCard';
import { TaskFormModal } from '@/components/tasks/TaskFormModal';
import { useTasks } from '@/hooks/useTasks';
import { Pagination } from '@/components/common/Pagination';
import { CreateTaskRequest, Task, TaskStatus } from '@/types/task.types';
import { CheckSquare, Plus, RefreshCw, Search, CheckCircle2, Clock, CircleAlert } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [selectedStatus, setSelectedStatus] = useState<TaskStatus | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(9);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  const {
    tasks,
    tasksPage,
    isLoading,
    isError,
    error,
    refetch,
    createTask,
    updateTask,
    updateTaskStatus,
    deleteTask,
    isCreating,
    isUpdating,
  } = useTasks({
    status: selectedStatus,
    search: searchQuery,
    page,
    size: pageSize,
  });

  const handleStatusFilterChange = (status: TaskStatus | 'ALL') => {
    setSelectedStatus(status);
    setPage(0);
  };

  const handleSearchChange = (query: string) => {
    setSearchQuery(query);
    setPage(0);
  };

  const handleOpenCreateModal = () => {
    setEditingTask(null);
    setIsFormOpen(true);
  };

  const handleOpenEditModal = (task: Task) => {
    setEditingTask(task);
    setIsFormOpen(true);
  };

  const handleFormSubmit = async (data: CreateTaskRequest) => {
    if (editingTask) {
      await updateTask({
        id: editingTask.id,
        data: {
          title: data.title,
          description: data.description,
          status: data.status || editingTask.status,
        },
      });
    } else {
      await createTask(data);
    }
  };

  const handleDeleteTask = async (id: number) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cette tâche ?')) {
      try {
        await deleteTask(id);
      } catch (err) {
        console.error('Erreur lors de la suppression:', err);
      }
    }
  };

  const handleStatusChange = async (id: number, status: TaskStatus) => {
    try {
      await updateTaskStatus({ id, data: { status } });
    } catch (err) {
      console.error('Erreur lors de la mise à jour du statut:', err);
    }
  };

  const statusTabs: { id: TaskStatus | 'ALL'; label: string; icon?: React.ReactNode }[] = [
    { id: 'ALL', label: 'Toutes' },
    { id: 'TODO', label: 'À faire', icon: <Clock className="w-3.5 h-3.5" /> },
    { id: 'IN_PROGRESS', label: 'En cours', icon: <CircleAlert className="w-3.5 h-3.5" /> },
    { id: 'DONE', label: 'Terminées', icon: <CheckCircle2 className="w-3.5 h-3.5" /> },
  ];

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Top Control Bar */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 glass-panel p-6 rounded-3xl">
          <div className="space-y-1">
            <h2 className="text-xl font-bold text-slate-100 tracking-tight">Tableau de bord</h2>
            <p className="text-xs text-slate-400">
              {tasksPage?.totalElements ?? 0} tâche(s) au total
            </p>
          </div>

          <Button
            onClick={handleOpenCreateModal}
            icon={<Plus className="w-4 h-4" />}
            size="md"
          >
            Nouvelle tâche
          </Button>
        </div>

        {/* Filters and Search Bar */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          {/* Status Tabs */}
          <div className="flex items-center gap-1.5 bg-slate-900/80 p-1.5 rounded-2xl border border-slate-800/80 w-full sm:w-auto overflow-x-auto">
            {statusTabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => handleStatusFilterChange(tab.id)}
                className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold transition-all duration-200 whitespace-nowrap ${
                  selectedStatus === tab.id
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/25'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                }`}
              >
                {tab.icon}
                <span>{tab.label}</span>
              </button>
            ))}
          </div>

          {/* Search Box */}
          <div className="w-full sm:w-72">
            <Input
              placeholder="Rechercher une tâche..."
              icon={<Search className="w-4 h-4" />}
              value={searchQuery}
              onChange={(e) => handleSearchChange(e.target.value)}
            />
          </div>
        </div>

        {/* Error State */}
        {isError && (
          <div className="space-y-4">
            <Alert
              type="error"
              title="Erreur de chargement"
              message={(error as any)?.response?.data?.message || 'Impossible de récupérer la liste des tâches.'}
            />
            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              icon={<RefreshCw className="w-4 h-4" />}
            >
              Réessayer
            </Button>
          </div>
        )}

        {/* Loading Skeleton State */}
        {isLoading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((n) => (
              <div
                key={n}
                className="glass-panel p-6 rounded-2xl space-y-4 animate-pulse"
              >
                <div className="h-5 bg-slate-800/80 rounded-lg w-3/4" />
                <div className="space-y-2">
                  <div className="h-3 bg-slate-800/50 rounded-lg w-full" />
                  <div className="h-3 bg-slate-800/50 rounded-lg w-5/6" />
                </div>
                <div className="pt-4 border-t border-slate-800/60 flex items-center justify-between">
                  <div className="h-3 bg-slate-800/40 rounded-lg w-1/3" />
                  <div className="h-6 bg-slate-800/60 rounded-full w-16" />
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Empty State */}
        {!isLoading && !isError && tasks.length === 0 && (
          <div className="glass-panel p-12 rounded-3xl text-center space-y-4 max-w-md mx-auto my-12">
            <div className="w-16 h-16 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-center mx-auto text-slate-500">
              <CheckSquare className="w-8 h-8" />
            </div>
            <div className="space-y-1">
              <h3 className="text-base font-semibold text-slate-200">
                Aucune tâche trouvée
              </h3>
              <p className="text-xs text-slate-400">
                {searchQuery || selectedStatus !== 'ALL'
                  ? 'Aucune tâche ne correspond à vos critères de recherche.'
                  : 'Vous n\'avez pas encore créé de tâche. Commencez dès maintenant !'}
              </p>
            </div>
            {selectedStatus === 'ALL' && !searchQuery && (
              <Button onClick={handleOpenCreateModal} icon={<Plus className="w-4 h-4" />}>
                Créer ma première tâche
              </Button>
            )}
          </div>
        )}

        {/* Task Grid State */}
        {!isLoading && !isError && tasks.length > 0 && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {tasks.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  onEdit={handleOpenEditModal}
                  onDelete={handleDeleteTask}
                  onStatusChange={handleStatusChange}
                />
              ))}
            </div>

            {/* Pagination Controls */}
            {tasksPage && (
              <Pagination
                currentPage={tasksPage.pageNumber}
                totalPages={tasksPage.totalPages}
                totalElements={tasksPage.totalElements}
                pageSize={pageSize}
                onPageChange={(newPage) => setPage(newPage)}
                onPageSizeChange={(newSize) => {
                  setPageSize(newSize);
                  setPage(0);
                }}
              />
            )}
          </div>
        )}
      </main>

      {/* Task Creation & Edition Modal */}
      <TaskFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleFormSubmit}
        taskToEdit={editingTask}
        isLoading={isCreating || isUpdating}
      />
    </div>
  );
};

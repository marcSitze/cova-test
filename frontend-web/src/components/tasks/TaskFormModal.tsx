import React, { useEffect, useState } from 'react';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { CreateTaskRequest, Task, TaskStatus } from '@/types/task.types';

interface TaskFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateTaskRequest) => Promise<void>;
  taskToEdit?: Task | null;
  isLoading?: boolean;
}

export const TaskFormModal: React.FC<TaskFormModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  taskToEdit,
  isLoading = false,
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [errors, setErrors] = useState<{ title?: string }>({});

  useEffect(() => {
    if (taskToEdit) {
      setTitle(taskToEdit.title);
      setDescription(taskToEdit.description || '');
      setStatus(taskToEdit.status);
    } else {
      setTitle('');
      setDescription('');
      setStatus('TODO');
    }
    setErrors({});
  }, [taskToEdit, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrors({ title: 'Le titre de la tâche est obligatoire.' });
      return;
    }
    if (title.length > 150) {
      setErrors({ title: 'Le titre ne peut pas dépasser 150 caractères.' });
      return;
    }

    try {
      await onSubmit({
        title: title.trim(),
        description: description.trim() || undefined,
        status,
      });
      onClose();
    } catch (err) {
      console.error('Erreur lors de la soumission de la tâche:', err);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={taskToEdit ? 'Modifier la tâche' : 'Nouvelle tâche'}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Titre de la tâche *"
          placeholder="Ex: Rédiger la documentation API"
          value={title}
          onChange={(e) => {
            setTitle(e.target.value);
            if (errors.title) setErrors({});
          }}
          error={errors.title}
          disabled={isLoading}
          autoFocus
        />

        <div className="space-y-1.5">
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
            Description
          </label>
          <textarea
            rows={4}
            className="w-full rounded-xl glass-input px-4 py-2.5 text-sm resize-none"
            placeholder="Détails complémentaires sur la tâche (optionnel)..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={isLoading}
          />
        </div>

        <div className="space-y-1.5">
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
            Statut
          </label>
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value as TaskStatus)}
            className="w-full rounded-xl glass-input px-4 py-2.5 text-sm appearance-none bg-slate-900 border border-slate-800 text-slate-100"
            disabled={isLoading}
          >
            <option value="TODO">À faire</option>
            <option value="IN_PROGRESS">En cours</option>
            <option value="DONE">Terminée</option>
          </select>
        </div>

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800/80">
          <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
            Annuler
          </Button>
          <Button type="submit" isLoading={isLoading}>
            {taskToEdit ? 'Enregistrer les modifications' : 'Créer la tâche'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

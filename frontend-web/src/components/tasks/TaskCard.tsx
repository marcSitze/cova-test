import React from 'react';
import { Badge } from '@/components/common/Badge';
import { Task, TaskStatus } from '@/types/task.types';
import { Calendar, Edit2, Trash2 } from 'lucide-react';

interface TaskCardProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (id: number) => void;
  onStatusChange: (id: number, status: TaskStatus) => void;
}

export const TaskCard: React.FC<TaskCardProps> = ({
  task,
  onEdit,
  onDelete,
  onStatusChange,
}) => {
  const formattedDate = new Date(task.createdAt).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });

  return (
    <div className="glass-panel p-5 rounded-2xl hover:border-slate-700/80 transition-all duration-300 flex flex-col justify-between gap-4 group">
      <div className="space-y-2.5">
        <div className="flex items-start justify-between gap-3">
          <h3 className="font-semibold text-slate-100 text-base group-hover:text-indigo-400 transition-colors line-clamp-1">
            {task.title}
          </h3>
          <Badge status={task.status} />
        </div>

        {task.description && (
          <p className="text-slate-400 text-xs leading-relaxed line-clamp-3">
            {task.description}
          </p>
        )}
      </div>

      <div className="pt-3 border-t border-slate-800/60 flex items-center justify-between text-xs text-slate-500">
        <div className="flex items-center gap-1.5">
          <Calendar className="w-3.5 h-3.5" />
          <span>Créée le {formattedDate}</span>
        </div>

        <div className="flex items-center gap-1">
          <select
            value={task.status}
            onChange={(e) => onStatusChange(task.id, e.target.value as TaskStatus)}
            className="bg-slate-900 border border-slate-800 text-slate-300 text-xs rounded-lg px-2 py-1 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          >
            <option value="TODO">À faire</option>
            <option value="IN_PROGRESS">En cours</option>
            <option value="DONE">Terminée</option>
          </select>

          <button
            onClick={() => onEdit(task)}
            className="p-1.5 text-slate-400 hover:text-indigo-400 hover:bg-indigo-500/10 rounded-lg transition-colors"
            title="Modifier"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>

          <button
            onClick={() => onDelete(task.id)}
            className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
            title="Supprimer"
          >
            <Trash2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
};

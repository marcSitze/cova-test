import React from 'react';
import { TaskStatus } from '@/types/task.types';
import { CheckCircle2, Clock, CircleAlert } from 'lucide-react';

interface BadgeProps {
  status: TaskStatus;
}

export const Badge: React.FC<BadgeProps> = ({ status }) => {
  const config = {
    TODO: {
      label: 'À faire',
      bg: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
      icon: <Clock className="w-3.5 h-3.5" />,
    },
    IN_PROGRESS: {
      label: 'En cours',
      bg: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30',
      icon: <CircleAlert className="w-3.5 h-3.5" />,
    },
    DONE: {
      label: 'Terminée',
      bg: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
      icon: <CheckCircle2 className="w-3.5 h-3.5" />,
    },
  };

  const current = config[status] || config.TODO;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border ${current.bg} transition-all duration-200`}
    >
      {current.icon}
      <span>{current.label}</span>
    </span>
  );
};

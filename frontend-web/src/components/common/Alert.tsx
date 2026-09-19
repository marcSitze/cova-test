import React from 'react';
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';

interface AlertProps {
  type?: 'error' | 'success' | 'info';
  title?: string;
  message: string;
  onClose?: () => void;
}

export const Alert: React.FC<AlertProps> = ({
  type = 'error',
  title,
  message,
  onClose,
}) => {
  const styles = {
    error: {
      bg: 'bg-rose-500/10 border-rose-500/30 text-rose-300',
      icon: <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />,
    },
    success: {
      bg: 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300',
      icon: <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />,
    },
    info: {
      bg: 'bg-indigo-500/10 border-indigo-500/30 text-indigo-300',
      icon: <Info className="w-5 h-5 text-indigo-400 shrink-0" />,
    },
  };

  const current = styles[type];

  return (
    <div className={`flex items-start justify-between p-4 rounded-xl border ${current.bg} gap-3 transition-all duration-200`}>
      <div className="flex items-start gap-3">
        {current.icon}
        <div className="space-y-0.5">
          {title && <h4 className="text-sm font-semibold">{title}</h4>}
          <p className="text-xs leading-relaxed">{message}</p>
        </div>
      </div>
      {onClose && (
        <button
          onClick={onClose}
          className="text-slate-400 hover:text-slate-100 transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
};

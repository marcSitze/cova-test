import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { TaskCard } from './TaskCard';
import { Task } from '@/types/task.types';

describe('TaskCard Component', () => {
  const mockTask: Task = {
    id: 1,
    title: 'Tester le Frontend React',
    description: 'Vérifier le bon fonctionnement des tests Vitest',
    status: 'TODO',
    createdAt: '2026-09-17T20:00:00Z',
    updatedAt: '2026-09-17T20:00:00Z',
  };

  it('renders task title and description', () => {
    render(
      <TaskCard
        task={mockTask}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
        onStatusChange={vi.fn()}
      />
    );

    expect(screen.getByText('Tester le Frontend React')).toBeInTheDocument();
    expect(screen.getByText('Vérifier le bon fonctionnement des tests Vitest')).toBeInTheDocument();
  });

  it('triggers onEdit callback when clicking edit button', () => {
    const handleEdit = vi.fn();
    render(
      <TaskCard
        task={mockTask}
        onEdit={handleEdit}
        onDelete={vi.fn()}
        onStatusChange={vi.fn()}
      />
    );

    const editBtn = screen.getByTitle('Modifier');
    fireEvent.click(editBtn);

    expect(handleEdit).toHaveBeenCalledWith(mockTask);
  });

  it('triggers onDelete callback when clicking delete button', () => {
    const handleDelete = vi.fn();
    render(
      <TaskCard
        task={mockTask}
        onEdit={vi.fn()}
        onDelete={handleDelete}
        onStatusChange={vi.fn()}
      />
    );

    const deleteBtn = screen.getByTitle('Supprimer');
    fireEvent.click(deleteBtn);

    expect(handleDelete).toHaveBeenCalledWith(1);
  });
});

import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { Badge } from './Badge';

describe('Badge Component', () => {
  it('renders "À faire" label for TODO status', () => {
    render(<Badge status="TODO" />);
    expect(screen.getByText('À faire')).toBeInTheDocument();
  });

  it('renders "En cours" label for IN_PROGRESS status', () => {
    render(<Badge status="IN_PROGRESS" />);
    expect(screen.getByText('En cours')).toBeInTheDocument();
  });

  it('renders "Terminée" label for DONE status', () => {
    render(<Badge status="DONE" />);
    expect(screen.getByText('Terminée')).toBeInTheDocument();
  });
});

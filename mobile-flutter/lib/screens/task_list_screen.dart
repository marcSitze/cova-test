import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/task_model.dart';
import '../providers/auth_provider.dart';
import '../providers/task_provider.dart';
import 'task_form_screen.dart';

class TaskListScreen extends StatefulWidget {
  const TaskListScreen({super.key});

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

class _TaskListScreenState extends State<TaskListScreen> {
  final _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<TaskProvider>(context, listen: false).fetchTasks();
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _openTaskForm([TaskModel? task]) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => TaskFormScreen(taskToEdit: task),
    );
  }

  void _confirmDelete(BuildContext context, int taskId) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Supprimer la tâche'),
        content: const Text('Êtes-vous sûr de vouloir supprimer cette tâche ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFE11D48)),
            onPressed: () async {
              Navigator.pop(ctx);
              final provider = Provider.of<TaskProvider>(context, listen: false);
              await provider.deleteTask(taskId);
            },
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );
  }

  Color _getStatusColor(TaskStatus status) {
    switch (status) {
      case TaskStatus.TODO:
        return const Color(0xFFD97706);
      case TaskStatus.IN_PROGRESS:
        return const Color(0xFF818CF8);
      case TaskStatus.DONE:
        return const Color(0xFF10B981);
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = Provider.of<AuthProvider>(context);
    final taskProvider = Provider.of<TaskProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Task Manager'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Déconnexion',
            onPressed: () => authProvider.logout(),
          ),
        ],
      ),
      body: Column(
        children: [
          // Filter & Search Header
          Container(
            padding: const EdgeInsets.all(16.0),
            color: const Color(0xFF0F172A),
            child: Column(
              children: [
                TextField(
                  controller: _searchController,
                  decoration: InputDecoration(
                    hintText: 'Rechercher une tâche...',
                    prefixIcon: const Icon(Icons.search),
                    suffixIcon: _searchController.text.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear),
                            onPressed: () {
                              _searchController.clear();
                              taskProvider.setSearchQuery('');
                            },
                          )
                        : null,
                  ),
                  onChanged: (value) => taskProvider.setSearchQuery(value),
                ),
                const SizedBox(height: 12),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: [
                      _buildFilterChip(context, null, 'Toutes'),
                      const SizedBox(width: 8),
                      _buildFilterChip(context, TaskStatus.TODO, 'À faire'),
                      const SizedBox(width: 8),
                      _buildFilterChip(context, TaskStatus.IN_PROGRESS, 'En cours'),
                      const SizedBox(width: 8),
                      _buildFilterChip(context, TaskStatus.DONE, 'Terminées'),
                    ],
                  ),
                ),
              ],
            ),
          ),

          // Task List Body
          Expanded(
            child: taskProvider.isLoading
                ? const Center(child: CircularProgressIndicator())
                : taskProvider.errorMessage != null
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              taskProvider.errorMessage!,
                              style: const TextStyle(color: Color(0xFFF43F5E)),
                              textAlign: TextAlign.center,
                            ),
                            const SizedBox(height: 12),
                            ElevatedButton(
                              onPressed: () => taskProvider.fetchTasks(),
                              child: const Text('Réessayer'),
                            ),
                          ],
                        ),
                      )
                    : taskProvider.tasks.isEmpty
                        ? Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const Icon(Icons.assignment_outlined, size: 64, color: Color(0xFF475569)),
                                const SizedBox(height: 16),
                                const Text(
                                  'Aucune tâche trouvée',
                                  style: TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                    color: Color(0xFFCBD5E1),
                                  ),
                                ),
                              ],
                            ),
                          )
                        : RefreshIndicator(
                            onRefresh: () => taskProvider.fetchTasks(),
                            child: ListView.builder(
                              padding: const EdgeInsets.all(16),
                              itemCount: taskProvider.tasks.length,
                              itemBuilder: (ctx, index) {
                                final task = taskProvider.tasks[index];
                                return Card(
                                  margin: const EdgeInsets.only(bottom: 12),
                                  child: ListTile(
                                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                                    title: Text(
                                      task.title,
                                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                                    ),
                                    subtitle: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        if (task.description != null && task.description!.isNotEmpty) ...[
                                          const SizedBox(height: 4),
                                          Text(
                                            task.description!,
                                            maxLines: 2,
                                            overflow: TextOverflow.ellipsis,
                                            style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13),
                                          ),
                                        ],
                                        const SizedBox(height: 8),
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                          decoration: BoxDecoration(
                                            color: _getStatusColor(task.status).withOpacity(0.15),
                                            borderRadius: BorderRadius.circular(8),
                                            border: Border.all(color: _getStatusColor(task.status).withOpacity(0.5)),
                                          ),
                                          child: Text(
                                            task.status.label,
                                            style: TextStyle(
                                              color: _getStatusColor(task.status),
                                              fontSize: 11,
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                        ),
                                      ],
                                    ),
                                    trailing: Row(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        PopupMenuButton<TaskStatus>(
                                          icon: const Icon(Icons.more_vert),
                                          onSelected: (newStatus) {
                                            taskProvider.updateTaskStatus(task.id, newStatus);
                                          },
                                          itemBuilder: (ctx) => [
                                            const PopupMenuItem(
                                              value: TaskStatus.TODO,
                                              child: Text('À faire'),
                                            ),
                                            const PopupMenuItem(
                                              value: TaskStatus.IN_PROGRESS,
                                              child: Text('En cours'),
                                            ),
                                            const PopupMenuItem(
                                              value: TaskStatus.DONE,
                                              child: Text('Terminée'),
                                            ),
                                          ],
                                        ),
                                        IconButton(
                                          icon: const Icon(Icons.edit_outlined, size: 20),
                                          onPressed: () => _openTaskForm(task),
                                        ),
                                        IconButton(
                                          icon: const Icon(Icons.delete_outline, size: 20, color: Color(0xFFF43F5E)),
                                          onPressed: () => _confirmDelete(context, task.id),
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              },
                            ),
                          ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openTaskForm(),
        backgroundColor: const Color(0xFF4F46E5),
        icon: const Icon(Icons.add),
        label: const Text('Nouvelle tâche'),
      ),
    );
  }

  Widget _buildFilterChip(BuildContext context, TaskStatus? status, String label) {
    final taskProvider = Provider.of<TaskProvider>(context);
    final isSelected = taskProvider.selectedStatus == status;

    return FilterChip(
      selected: isSelected,
      label: Text(label),
      selectedColor: const Color(0xFF4F46E5),
      backgroundColor: const Color(0xFF0F172A),
      labelStyle: TextStyle(
        color: isSelected ? Colors.white : const Color(0xFF94A3B8),
        fontSize: 12,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
      ),
      onSelected: (_) => taskProvider.setStatusFilter(status),
    );
  }
}

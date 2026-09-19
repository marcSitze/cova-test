import 'package:flutter/material.dart';
import '../models/task_model.dart';
import '../repositories/task_repository.dart';

class TaskProvider extends ChangeNotifier {
  final TaskRepository _taskRepository;

  List<TaskModel> _tasks = [];
  TaskStatus? _selectedStatus;
  String _searchQuery = '';
  bool _isLoading = false;
  String? _errorMessage;

  TaskProvider(this._taskRepository);

  List<TaskModel> get tasks => _tasks;
  TaskStatus? get selectedStatus => _selectedStatus;
  String get searchQuery => _searchQuery;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> fetchTasks() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      _tasks = await _taskRepository.getTasks(
        status: _selectedStatus,
        search: _searchQuery,
      );
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      _isLoading = false;
      notifyListeners();
    }
  }

  void setStatusFilter(TaskStatus? status) {
    if (_selectedStatus != status) {
      _selectedStatus = status;
      fetchTasks();
    }
  }

  void setSearchQuery(String query) {
    _searchQuery = query;
    fetchTasks();
  }

  Future<bool> createTask(String title, String? description, TaskStatus status) async {
    try {
      await _taskRepository.createTask(title, description, status);
      await fetchTasks();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return false;
    }
  }

  Future<bool> updateTask(int id, String title, String? description, TaskStatus status) async {
    try {
      await _taskRepository.updateTask(id, title, description, status);
      await fetchTasks();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return false;
    }
  }

  Future<bool> updateTaskStatus(int id, TaskStatus status) async {
    try {
      await _taskRepository.updateTaskStatus(id, status);
      await fetchTasks();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteTask(int id) async {
    try {
      await _taskRepository.deleteTask(id);
      await fetchTasks();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      notifyListeners();
      return false;
    }
  }
}

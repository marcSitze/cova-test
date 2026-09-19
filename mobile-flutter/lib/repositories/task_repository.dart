import 'package:dio/dio.dart';
import '../core/constants/api_constants.dart';
import '../core/network/api_client.dart';
import '../models/task_model.dart';

class TaskRepository {
  final ApiClient _apiClient;

  TaskRepository(this._apiClient);

  Future<List<TaskModel>> getTasks({
    TaskStatus? status,
    String? search,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final queryParams = <String, dynamic>{
        'page': page,
        'size': size,
        'sort': 'createdAt,desc',
      };
      if (status != null) {
        queryParams['status'] = status.value;
      }
      if (search != null && search.isNotEmpty) {
        queryParams['search'] = search;
      }

      final response = await _apiClient.dio.get(
        ApiConstants.tasks,
        queryParameters: queryParams,
      );

      final content = response.data['content'] as List<dynamic>;
      return content.map((json) => TaskModel.fromJson(json as Map<String, dynamic>)).toList();
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? 'Impossible de charger la liste des tâches.';
      throw Exception(message);
    }
  }

  Future<TaskModel> createTask(String title, String? description, TaskStatus status) async {
    try {
      final response = await _apiClient.dio.post(
        ApiConstants.tasks,
        data: {
          'title': title,
          'description': description,
          'status': status.value,
        },
      );
      return TaskModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? 'Erreur lors de la création de la tâche.';
      throw Exception(message);
    }
  }

  Future<TaskModel> updateTask(int id, String title, String? description, TaskStatus status) async {
    try {
      final response = await _apiClient.dio.put(
        '${ApiConstants.tasks}/$id',
        data: {
          'title': title,
          'description': description,
          'status': status.value,
        },
      );
      return TaskModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? 'Erreur lors de la modification de la tâche.';
      throw Exception(message);
    }
  }

  Future<TaskModel> updateTaskStatus(int id, TaskStatus status) async {
    try {
      final response = await _apiClient.dio.patch(
        '${ApiConstants.tasks}/$id/status',
        data: {
          'status': status.value,
        },
      );
      return TaskModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? 'Erreur lors de la mise à jour du statut.';
      throw Exception(message);
    }
  }

  Future<void> deleteTask(int id) async {
    try {
      await _apiClient.dio.delete('${ApiConstants.tasks}/$id');
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? 'Erreur lors de la suppression de la tâche.';
      throw Exception(message);
    }
  }
}

import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_mobile/models/task_model.dart';

void main() {
  group('TaskModel Serialization Tests', () {
    test('TaskModel.fromJson parses JSON accurately', () {
      final json = {
        'id': 101,
        'title': 'Test Flutter Unit Test',
        'description': 'Description de test Dart',
        'status': 'IN_PROGRESS',
        'createdAt': '2026-09-17T20:00:00Z',
        'updatedAt': '2026-09-17T20:00:00Z',
      };

      final task = TaskModel.fromJson(json);

      expect(task.id, 101);
      expect(task.title, 'Test Flutter Unit Test');
      expect(task.description, 'Description de test Dart');
      expect(task.status, TaskStatus.IN_PROGRESS);
      expect(task.status.label, 'En cours');
    });

    test('TaskModel.toJson produces matching JSON map', () {
      final task = TaskModel(
        id: 202,
        title: 'Nouvelle tâche',
        description: 'Detail',
        status: TaskStatus.DONE,
        createdAt: '2026-09-17T20:00:00Z',
        updatedAt: '2026-09-17T20:00:00Z',
      );

      final json = task.toJson();

      expect(json['id'], 202);
      expect(json['title'], 'Nouvelle tâche');
      expect(json['status'], 'DONE');
    });
  });
}

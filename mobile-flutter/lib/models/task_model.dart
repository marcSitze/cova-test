enum TaskStatus { TODO, IN_PROGRESS, DONE }

extension TaskStatusExtension on TaskStatus {
  String get value {
    switch (this) {
      case TaskStatus.TODO:
        return 'TODO';
      case TaskStatus.IN_PROGRESS:
        return 'IN_PROGRESS';
      case TaskStatus.DONE:
        return 'DONE';
    }
  }

  String get label {
    switch (this) {
      case TaskStatus.TODO:
        return 'À faire';
      case TaskStatus.IN_PROGRESS:
        return 'En cours';
      case TaskStatus.DONE:
        return 'Terminée';
    }
  }

  static TaskStatus fromString(String status) {
    switch (status) {
      case 'IN_PROGRESS':
        return TaskStatus.IN_PROGRESS;
      case 'DONE':
        return TaskStatus.DONE;
      case 'TODO':
      default:
        return TaskStatus.TODO;
    }
  }
}

class TaskModel {
  final int id;
  final String title;
  final String? description;
  final TaskStatus status;
  final String createdAt;
  final String updatedAt;

  TaskModel({
    required this.id,
    required this.title,
    this.description,
    required this.status,
    required this.createdAt,
    required this.updatedAt,
  });

  factory TaskModel.fromJson(Map<String, dynamic> json) {
    return TaskModel(
      id: json['id'] as int,
      title: json['title'] as String,
      description: json['description'] as String?,
      status: TaskStatusExtension.fromString(json['status'] as String),
      createdAt: json['createdAt'] as String,
      updatedAt: json['updatedAt'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'status': status.value,
      'createdAt': createdAt,
      'updatedAt': updatedAt,
    };
  }
}

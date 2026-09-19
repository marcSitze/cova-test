import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/network/api_client.dart';
import 'core/theme/app_theme.dart';
import 'providers/auth_provider.dart';
import 'providers/task_provider.dart';
import 'repositories/auth_repository.dart';
import 'repositories/task_repository.dart';
import 'screens/login_screen.dart';
import 'screens/task_list_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  late final ApiClient apiClient;
  late final AuthRepository authRepository;
  late final TaskRepository taskRepository;

  apiClient = ApiClient(onUnauthorized: () {
    // Callback automatique lors d'une expiration 401
  });

  authRepository = AuthRepository(apiClient);
  taskRepository = TaskRepository(apiClient);

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider<AuthProvider>(
          create: (_) => AuthProvider(authRepository),
        ),
        ChangeNotifierProvider<TaskProvider>(
          create: (_) => TaskProvider(taskRepository),
        ),
      ],
      child: const TaskManagerApp(),
    ),
  );
}

class TaskManagerApp extends StatelessWidget {
  const TaskManagerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Task Manager',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme,
      home: Consumer<AuthProvider>(
        builder: (context, auth, _) {
          if (auth.isLoading) {
            return const Scaffold(
              body: Center(
                child: CircularProgressIndicator(),
              ),
            );
          }
          if (auth.isAuthenticated) {
            return const TaskListScreen();
          }
          return const LoginScreen();
        },
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../repositories/auth_repository.dart';

class AuthProvider extends ChangeNotifier {
  final AuthRepository _authRepository;
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  String? _token;
  String? _userEmail;
  bool _isLoading = true;
  String? _errorMessage;

  AuthProvider(this._authRepository) {
    checkAuthStatus();
  }

  String? get token => _token;
  String? get userEmail => _userEmail;
  bool get isAuthenticated => _token != null && _token!.isNotEmpty;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> checkAuthStatus() async {
    _isLoading = true;
    notifyListeners();

    _token = await _secureStorage.read(key: 'jwt_token');
    _userEmail = await _secureStorage.read(key: 'user_email');

    _isLoading = false;
    notifyListeners();
  }

  Future<bool> login(String email, String password) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final authResponse = await _authRepository.login(email, password);
      _token = authResponse.token;
      _userEmail = authResponse.user.email;

      await _secureStorage.write(key: 'jwt_token', value: _token);
      await _secureStorage.write(key: 'user_email', value: _userEmail);

      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> register(String email, String password) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final authResponse = await _authRepository.register(email, password);
      _token = authResponse.token;
      _userEmail = authResponse.user.email;

      await _secureStorage.write(key: 'jwt_token', value: _token);
      await _secureStorage.write(key: 'user_email', value: _userEmail);

      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    _token = null;
    _userEmail = null;
    await _secureStorage.delete(key: 'jwt_token');
    await _secureStorage.delete(key: 'user_email');
    notifyListeners();
  }
}

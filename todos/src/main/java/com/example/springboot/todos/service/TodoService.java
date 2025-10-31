package com.example.springboot.todos.service;

import com.example.springboot.todos.request.TodoRequest;
import com.example.springboot.todos.response.TodoResponse;

import java.util.List;

public interface TodoService {
    TodoResponse createTodo(TodoRequest todoRequest);
    List<TodoResponse> getAllTodos();
    TodoResponse toggleTodoCompletion(long id);
    void deleteTodo(long id);

}

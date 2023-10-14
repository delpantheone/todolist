package br.com.gabrielpaulo.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gabrielpaulo.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel,
            HttpServletRequest request) {
        var userID = request.getAttribute("userID");
        taskModel.setUserID((UUID) userID);
        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) ||
                currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início / término deve ser maior que a data presente");
        }
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início deve ser menor que a data de término");
        }
        var createdTask = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(createdTask);
    }

    // Exemplo de @JJMagnani
    // public TaskModel create(@RequestBody TaskModel taskModeL) {
    // var task = this.taskRepository.save(taskModeL);
    // System.out.println(task);
    // return task;
    // }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var userID = request.getAttribute("userID");
        var tasks = this.taskRepository.findByUserID((UUID) userID);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var task = this.taskRepository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada");
        }
        var userID = request.getAttribute("userID");
        if (!task.getUserID().equals(userID)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar essa tarefa");
        }
        Utils.copyNonNullProperties(taskModel, task);
        var updatedTask = this.taskRepository.save(task);
        return ResponseEntity.ok().body(updatedTask);
    }
}

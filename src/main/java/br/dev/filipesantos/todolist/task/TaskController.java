package br.dev.filipesantos.todolist.task;

import br.dev.filipesantos.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @RequestMapping("/create")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        var currentDate = LocalDateTime.now();

        taskModel.setIdUser((UUID) idUser);

        if(currentDate.isAfter(taskModel.getEndAt()) || currentDate.isAfter(taskModel.getStartAt()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date cannot be in the future.");

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date cannot be after end date.");

        var task = this.taskRepository.save(taskModel);

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/list")
    public List<TaskModel> list(HttpServletRequest request){
        var idUser = request.getAttribute("idUser");

        return this.taskRepository.findByIdUser((UUID) idUser);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){
        var task = this.taskRepository.findById(id).orElse(null);
        var idUser = request.getAttribute("idUser");

        if(task == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task not found.");

        if(!task.getIdUser().equals((idUser)))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are not authorized to update this task.");

        Utils.copyNonNullProperties(taskModel, task);

        return ResponseEntity.ok().body(this.taskRepository.save(task));
    }


}

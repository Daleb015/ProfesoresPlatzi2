package com.platzi.profesoresplatzi.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.platzi.profesoresplatzi.model.Course;
import com.platzi.profesoresplatzi.service.CourseService;
import com.platzi.profesoresplatzi.util.CustomErrorType;

@Controller
@RequestMapping("/v1")
public class CourseController {

  @Autowired
  CourseService courseService;

  @RequestMapping(value = "/courses", method = RequestMethod.GET, headers = "Accept=application/json")
  private ResponseEntity<List<Course>> getCourses(
      @RequestParam(value = "name", required = false) String name) {
    List<Course> courses = new ArrayList<>();

    if (name == null) {
      courses = courseService.findAllCourses();
      if (courses.isEmpty()) {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<List<Course>>(courses, HttpStatus.OK);
    } else {
      Course course = courseService.findByName(name);
      if (course == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      courses.add(course);
      return new ResponseEntity<List<Course>>(courses, HttpStatus.OK);
    }

  }

  @RequestMapping(value = "/courses", method = RequestMethod.POST, headers = "Accept=application/json")
  public ResponseEntity<?> createCourse(@RequestBody Course course,
      UriComponentsBuilder uriComponentsBuilder) {
    if (course.getName().equals(null) || course.getName().isEmpty()) {
      return new ResponseEntity(new CustomErrorType("El campo nombre no es valido"),
          HttpStatus.CONFLICT);
    }

    if (courseService.findByName(course.getName()) != null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    courseService.saveCourse(course);
    Course course2 = courseService.findByName(course.getName());
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponentsBuilder.path("/v1/courses/{id}")
        .buildAndExpand(course2.getIdCourse()).toUri());
    return new ResponseEntity<String>(headers, HttpStatus.CREATED);

  }

  @RequestMapping(value = "/courses/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
  public ResponseEntity<Course> getCourseById(@PathVariable("id") Long id) {
    if (id == null || id <= 0) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    Course course = courseService.findById(id);

    if (course == null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<Course>(course, HttpStatus.OK);
  }

  @RequestMapping(value = "/courses/{id}", method = RequestMethod.PATCH, headers = "Accept=application/json")
  public ResponseEntity<?> updateCourse(@PathVariable("id") Long id, @RequestBody Course course) {
    Course currentCourse = courseService.findById(id);

    if (currentCourse == null) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    currentCourse.setName(course.getName());
    currentCourse.setProject(course.getProject());
    currentCourse.setThemes(course.getThemes());

    courseService.updateCourse(currentCourse);
    return new ResponseEntity<Course>(currentCourse, HttpStatus.OK);

  }
  
  @RequestMapping(value="/courses/{id}",method=RequestMethod.DELETE)
  public ResponseEntity<?> deleteCourse(@PathVariable("id") Long id){
    if (id==null||id<=0) {
      return new ResponseEntity(new CustomErrorType("Campo id no valido"),HttpStatus.CONFLICT);
    }
    
    Course course = courseService.findById(id);
    
    if (course==null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT); 
    }
    
    courseService.deleteCourseById(id);
    return new ResponseEntity<Course>(HttpStatus.OK);
  }

}

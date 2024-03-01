package com.batchprocess.controler;
import com.batchprocess.entity.Customer;
import com.batchprocess.repository.CustomerRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/customers")
public class JobController {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;
    @Autowired
    private CustomerRepository customerRepository;
    private final String LOCAL_STORAGE = "C:/Users/TR624QQ/NewTech/batchStorage/";
    @GetMapping
    public List<Customer> getAllCustomers(){
        return customerRepository.findAll();
    }
    @PostMapping
    public String importData(@RequestParam("file") MultipartFile multipartFile){
        try{
            String OriginalFileName = multipartFile.getOriginalFilename();
            File actualFile = new File(LOCAL_STORAGE+OriginalFileName);
            multipartFile.transferTo(actualFile);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("fullPath",LOCAL_STORAGE+OriginalFileName)
                    .addLong("startAt", System.currentTimeMillis()).toJobParameters();

                jobLauncher.run(job, jobParameters);
            } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                     JobParametersInvalidException | IOException e) {
                e.printStackTrace();
            }
        return   "Data imported Successfully!" ;
    }

}

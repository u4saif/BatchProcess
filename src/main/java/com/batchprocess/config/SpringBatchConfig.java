package com.batchprocess.config;
import com.batchprocess.entity.Customer;
import com.batchprocess.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import java.io.File;

@Configuration
@AllArgsConstructor
public class SpringBatchConfig {

    private CustomerRepository customerRepository;

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{jobParameters[fullPath]}") String pathToFile){
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(new File(pathToFile)));
        itemReader.setName("fileCsvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }
    @Bean
    public LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fileSetMapper = new BeanWrapperFieldSetMapper<>();
        fileSetMapper.setTargetType(Customer.class);
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fileSetMapper);
        return lineMapper;
    }

    @Bean
    public CustomerProcessor processor(){
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> writer(){
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step stepFirst( FlatFileItemReader<Customer> reader,JobRepository jobRepository, TransactionManager transactionManager){
        return new StepBuilder("csv-step", jobRepository)
                .<Customer, Customer>chunk(500, (PlatformTransactionManager) transactionManager)
                .reader(reader)
                .processor(processor())
                .writer(writer())
                .build();

    }

    @Bean
    public Job runJob( FlatFileItemReader<Customer> reader,JobRepository jobRepository, TransactionManager transactionManager) {
        return new JobBuilder("customerImports", jobRepository)
                .start(stepFirst(reader,jobRepository,transactionManager))
                .build();
    }

}

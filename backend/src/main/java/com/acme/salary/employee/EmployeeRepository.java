package com.acme.salary.employee;

import com.acme.salary.insights.SalaryPoint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("select distinct e.country from Employee e order by e.country")
    List<String> findDistinctCountries();

    @Query("select distinct e.department from Employee e order by e.department")
    List<String> findDistinctDepartments();

    @Query("select distinct e.jobTitle from Employee e order by e.jobTitle")
    List<String> findDistinctJobTitles();

    /** Narrow projection for insights: only the columns the statistics need. */
    @Query("select new com.acme.salary.insights.SalaryPoint(e.country, e.department, e.jobTitle, e.currency, e.salary) from Employee e")
    List<SalaryPoint> findAllSalaryPoints();

    @Query("select new com.acme.salary.insights.SalaryPoint(e.country, e.department, e.jobTitle, e.currency, e.salary) from Employee e where e.country = :country")
    List<SalaryPoint> findSalaryPointsByCountry(@Param("country") String country);
}

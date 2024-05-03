package com.rihab.interventions.repos;


import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rihab.interventions.dto.TicketDTO;
import com.rihab.interventions.dto.UserDTO;
import com.rihab.interventions.entities.Equipement;
import com.rihab.interventions.entities.Intervention;
import com.rihab.interventions.entities.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, String> {

	List<Ticket> findByInterDesignation(String desing);
	 List<Ticket> findByInterDesignationContains(String desing); 
	 
	 
	 
	 List<Ticket> findByEquipementEqptCode(String eqtyCode);
	 List<Ticket>findByInterventionNatureCode(long code);
	// Ticket findByInterCode(String interCode) ;
	List<Ticket> findByTechnicienCodeTechnicien(long codeTechnicien);
	List<Ticket> findByDemandeurCodeDemandeur(long codeDemandeur);
	Long countByInterStatut(String string);
	Collection<UserDTO> findByDatePrevueAfter(Date currentDate);
	Optional<Ticket> findByInterCode(String interCode);
	List<Ticket> findByDatePrevueGreaterThanEqualAndDatePrevueLessThanEqual(Date startDate, Date endDate);
	
	List<Ticket> findByTechnicienCodeTechnicienAndInterStatutIn(long codeTechnicien, List<String> asList);
	//List<Ticket> findByInterventionInterClotureBeforeAndArchivedFalse(Date twoMonthsAgo);

	
	@Query("SELECT t FROM Ticket t JOIN t.intervention i WHERE t.demandeur.codeDemandeur = :codeDemandeur AND i.dateCloture < :limitDate")
	List<Ticket> findAllByDemandeurCodeDemandeurAndInterventionDateClotureBefore(@Param("codeDemandeur") long codeDemandeur, @Param("limitDate") Date limitDate);

	 
	
	@Query("SELECT t FROM Ticket t JOIN t.intervention i WHERE t.technicien.codeTechnicien = :codeTechnicien AND i.dateCloture < :limitDate ")
	List<Ticket> findAllByTechnicienCodeTechnicienAndInterventionDateClotureBefore(@Param("codeTechnicien") long codeTechnicien, @Param("limitDate") Date limitDate);
	
	
	@Query("SELECT t FROM Ticket t JOIN t.intervention i WHERE i.dateCloture < :limitDate")
	List<Ticket> findAllArchivedByInterventionDateClotureBefore(@Param("limitDate") Date limitDate);
	
	
	@Query("SELECT t FROM Ticket t JOIN t.intervention i WHERE i.dateCloture IS NULL OR i.dateCloture >= :limitDate")
	List<Ticket> findAllByInterventionDateClotureAfterOrEqualTo(@Param("limitDate") Date limitDate);

	@Query("SELECT t FROM Ticket t JOIN t.intervention i WHERE t.demandeur.codeDemandeur = :codeDemandeur AND (i.dateCloture IS NULL OR i.dateCloture >= :limitDate)")
	List<Ticket> findAllByDemandeurCodeDemandeur(@Param("codeDemandeur") long codeDemandeur, @Param("limitDate") Date limitDate);

	@Query("SELECT t FROM Ticket t JOIN t.intervention i WHERE t.technicien.codeTechnicien = :codeTechnicien AND (i.dateCloture IS NULL OR i.dateCloture >= :limitDate)")
	List<Ticket> findAllByTechnicienCodeTechnicien(@Param("codeTechnicien") long codeTechnicien, @Param("limitDate") Date limitDate);




}
	 



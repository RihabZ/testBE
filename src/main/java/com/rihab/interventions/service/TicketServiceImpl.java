package com.rihab.interventions.service;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.rihab.interventions.dto.DemandeurDTO;
import com.rihab.interventions.dto.EquipementDTO;
import com.rihab.interventions.dto.TicketDTO;
import com.rihab.interventions.dto.TicketStatisticsDTO;
import com.rihab.interventions.dto.UserDTO;
import com.rihab.interventions.entities.CalendarEvent;
import com.rihab.interventions.entities.Client;
import com.rihab.interventions.entities.Demandeur;
import com.rihab.interventions.entities.Equipement;
import com.rihab.interventions.entities.Intervention;
import com.rihab.interventions.entities.Technicien;
import com.rihab.interventions.entities.Ticket;
import com.rihab.interventions.entities.User;
import com.rihab.interventions.repos.TicketRepository;

import com.rihab.interventions.util.EmailService;


@Service
public class TicketServiceImpl implements TicketService {
	
	@Autowired
	TicketRepository ticketRepository;
	@Autowired
	DemandeurService demandeurService;
	@Autowired
	TechnicienService technicienService;
	 
	@Autowired
    private EmailService emailService;
	
	@Autowired
	private AuthenticationService userManagerService;
	@Autowired
	CalendarService calendarService;

@Autowired
ModelMap modelMapper;

/*
@Override
public TicketDTO saveTicket(TicketDTO inter)
{
	//inter.setInterCode(UUID.randomUUID());
return toTicketDTO(ticketRepository.save(toTicket(inter)));

}

*/
/*
public List<TicketDTO> getAllTicketsWithDemandeurDetails() {
    List<Ticket> tickets = ticketRepository.findAll();
    List<TicketDTO> ticketDTOs = new ArrayList<>();

    for (Ticket ticket : tickets) {
        TicketDTO ticketDTO = toTicketDTO(ticket);
        Demandeur demandeur = ticket.getDemandeur();
        if (demandeur != null) {
            UserDTO demandeurDTO = toUserDTO(demandeur.getUser()); // Convertir le demandeur en DTO
            ticketDTO.setDemandeurr(demandeurDTO);
        }
        ticketDTOs.add(ticketDTO);
    }

    return ticketDTOs;
}
public UserDTO toUserDTO(User user) {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(user.getId());
    userDTO.setNom(user.getNom());
    userDTO.setPrenom(user.getPrenom());
    userDTO.setEmail(user.getEmail());
    userDTO.setTel(user.getTel());
    userDTO.setAge(user.getAge());
    userDTO.setRole(user.getRole());
    userDTO.setSexe(user.getSexe());
    userDTO.setCodeDemandeur(user.getDemandeur().getCodeDemandeur());
    userDTO.setPost(user.getDemandeur().getPost());
    userDTO.setAdresse(user.getDemandeur().getClient().getAdresse());
    userDTO.setCodePostal(user.getDemandeur().getClient().getCodePostal());
    userDTO.setVille(user.getDemandeur().getClient().getVille());
    userDTO.setEmailSociete(user.getDemandeur().getClient().getEmailSociete());
    userDTO.setTell(user.getDemandeur().getClient().getTel());
    userDTO.setUsername(user.getUsername());
    userDTO.setCodeClient(user.getDemandeur().getClient().getCodeClient());
    userDTO.setDateEmbauche(user.getDateEmbauche());

    // Autres attributs à convertir selon vos besoins

    return userDTO;
}
*/
@Override
public TicketDTO saveTicket(TicketDTO ticketDTO) {
    // Get the authenticated user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
        // Handle unauthenticated users or return an error
        return null;
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String username = userDetails.getUsername();

    Demandeur demandeur = demandeurService.getDemandeurByUsername(username);

    if (demandeur == null) {
        // Handle the case where the demandeur doesn't exist
        return null;
    }

    // Convert DTO to entity
    Ticket ticket = toTicket(ticketDTO);
    ticket.setIntervention(ticket.getIntervention()); 
    ticket.setDemandeur(demandeur);
    ticket.setInterCode("I24-" + generateShortUUID().substring(0, 4));

    // Save the entity
    Ticket savedTicket = ticketRepository.save(ticket);

    // Send email to all managers
    List<UserDTO> managers = userManagerService.getAllManagers();
    for (UserDTO manager : managers) {
        emailService.sendNewTicketEmail(manager.getEmail(), demandeur.getUser().getNom() + " " + demandeur.getUser().getPrenom(), ticketDTO.getInterDesignation());
    }

    // Convert the saved entity to DTO
    TicketDTO savedTicketDTO = toTicketDTO(savedTicket);

    
    return savedTicketDTO;
}

private String generateShortUUID() {
    UUID uuid = UUID.randomUUID();
    long lsb = uuid.getLeastSignificantBits();
    long msb = uuid.getMostSignificantBits();
    return Long.toHexString(msb ^ lsb);
}
/*	
	@Override
	public List<TicketDTO> getAllTicketDTOs() {
	    List<Ticket> tickets = ticketRepository.findAll();
	    return tickets.stream()
	            .map(this::mapToTicketDTO)
	            .collect(Collectors.toList());
	}

	private TicketDTO mapToTicketDTO(Ticket ticket) {
	    TicketDTO dto = new TicketDTO();
	    dto.setInterCode((UUID.randomUUID()));
	    dto.setInterDesignation(ticket.getInterDesignation());
	    dto.setEquipement(mapToEquipementDTO(ticket.getEquipement()));
	   
	    // Map other fields if needed
	    return dto;
	}


public EquipementDTO mapToEquipementDTO(Equipement equipement) {
    EquipementDTO dto = new EquipementDTO();
    dto.setCode(equipement.getEqptCode());
    dto.setArticleCode(equipement.getArticleCode());
    dto.setCentreCode(equipement.getCentreCode());
   dto.setDateDemontage(equipement.getDateDemontage());
   dto.setDateFabrication(equipement.getDateFabrication());
   dto.setDateFinGarantie(equipement.getDateFinGarantie());
   dto.setDateInstallation(equipement.getDateInstallation());
   dto.setDateMiseEnService(equipement.getDateMiseEnService());
   dto.setDateRemplacement(equipement.getDateRemplacement());
   dto.setDesignation(equipement.getEqptDesignation());
   dto.setFamille(equipement.getFamille());
   dto.setEqptDtAchat(equipement.getEqptDtAchat());
   dto.setEqptDtCreation(equipement.getEqptDtCreation());
  dto.setDateLivraison(equipement.getDateLivraison());
   dto.setEqptGarantie(equipement.getEqptGarantie());
   dto.setEqptDureeGarantie(equipement.getEqptDureeGarantie());
   dto.setEqptPrix(equipement.getEqptPrix());
   dto.setEqptMachine(equipement.getEqptMachine());
dto.setType(equipement.getType());
dto.setPostCode(equipement.getPostCode());
dto.setRessCode(equipement.getRessCode());
dto.setSiteCode(equipement.getSiteCode());
dto.setEqptEnService(equipement.getEqptEnService());
dto.setEqptId(equipement.getEqptId());
dto.setEqptGarTypeDtRef(equipement.getEqptGarTypeDtRef());
dto.setEqptMachine(equipement.getEqptMachine());
// Map other fields
    return dto;
}
	
	
	*/
/*	
@Override
public TicketDTO updateTicket(TicketDTO inter) {
	return toTicketDTO(ticketRepository.save(toTicket(inter)));
}
*/@Override
public TicketDTO updateTicket(TicketDTO updatedTicketDTO) {
    // Vérifiez d'abord si le ticket à mettre à jour existe dans la base de données
    Optional<Ticket> optionalTicket = ticketRepository.findByInterCode(updatedTicketDTO.getInterCode());

    if (optionalTicket.isPresent()) {
        Ticket existingTicket = optionalTicket.get();

        // Mettez à jour les champs du ticket existant avec les nouvelles valeurs
        existingTicket.setInterDesignation(updatedTicketDTO.getInterDesignation());
        existingTicket.setInterPriorite(updatedTicketDTO.getInterPriorite());
        existingTicket.setInterStatut(updatedTicketDTO.getInterStatut());
        existingTicket.setMachineArret(updatedTicketDTO.getMachineArret());
        existingTicket.setDateArret(updatedTicketDTO.getDateArret());
        existingTicket.setDureeArret(updatedTicketDTO.getDureeArret());
        existingTicket.setDateCreation(updatedTicketDTO.getDateCreation());
        existingTicket.setDatePrevue(updatedTicketDTO.getDatePrevue());
        existingTicket.setDescription(updatedTicketDTO.getDescription());
        existingTicket.setSousContrat(updatedTicketDTO.getSousContrat());
        existingTicket.setSousGarantie(updatedTicketDTO.getSousGarantie());
        existingTicket.setEquipement(updatedTicketDTO.getEquipement());
        existingTicket.setDemandeur(updatedTicketDTO.getDemandeur());
        existingTicket.setTechnicien(updatedTicketDTO.getTechnicien());
        existingTicket.setInterventionNature(updatedTicketDTO.getInterventionNature());
        
        // Enregistrez les modifications dans la base de données
        Ticket updatedTicket = ticketRepository.save(existingTicket);

        // Si le statut du ticket est mis à "à réaliser", ajoutez ce ticket au calendrier
        if (updatedTicketDTO.getInterStatut().equals("à réaliser")) {
            addTicketToCalendar(updatedTicket);
        }

        // Convertissez le ticket mis à jour en DTO et renvoyez-le
        TicketDTO updatedTicketDTOResult = toTicketDTO(updatedTicket);
        return updatedTicketDTOResult;
    } else {
        // Gérez le cas où le ticket n'existe pas dans la base de données
        return null; // Ou lancez une exception appropriée
    }
}

    
 // Méthode pour ajouter un ticket au calendrier
    public void addTicketToCalendar(Ticket ticket) {
        // Vérifiez d'abord si le statut du ticket est "à réaliser"
        if (ticket.getInterStatut().equals("A réaliser")) {
            // Obtenez la date prévue du ticket
            Date datePrevue = ticket.getDatePrevue();

            // Convertir la date prévue en LocalDateTime
            LocalDateTime datePrevueLocalDateTime = datePrevue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            // Convertir la date prévue en Timestamp
            Timestamp datePrevueTimestamp = Timestamp.valueOf(datePrevueLocalDateTime);

            // Créer un nouvel événement calendrier avec les détails du ticket
            CalendarEvent event = new CalendarEvent(ticket.getInterCode(), ticket.getInterDesignation(), 
                                                    ticket.getDemandeur().getUser().getNom() + " " + ticket.getDemandeur().getUser().getPrenom(), 
                                                    datePrevueTimestamp);

            // Ajouter l'événement au calendrier
            calendarService.addNewTicketsToCalendar(List.of(ticket));
        }
    }

    
    
/*
existingTicket.getIntervention().setDateCloture(updatedTicketDTO.getDateCloture());
existingTicket.getIntervention().setDescriptionPanne(updatedTicketDTO.getDescriptionPanne());
existingTicket.getIntervention().setDtRealisation(updatedTicketDTO.getDtRealisation());
existingTicket.getIntervention().setDureeRealisation(updatedTicketDTO.getDureeRealisation());
existingTicket.getIntervention().setCompteRendu(updatedTicketDTO.getCompteRendu());
existingTicket.getIntervention().setInterventionObservation(updatedTicketDTO.getInterventionObservation());
existingTicket.getIntervention().setInterMtHebergement(updatedTicketDTO.getInterMtHebergement());
existingTicket.getIntervention().setInterMtDeplacement(updatedTicketDTO.getInterMtDeplacement());
existingTicket.getIntervention().setDifficulté(updatedTicketDTO.getDifficulté());
existingTicket.getIntervention().setInterventionType(updatedTicketDTO.getInterventionType());
existingTicket.getIntervention().setCause(updatedTicketDTO.getCause());
*/


/*
@Override
public TicketDTO updateTicketSelective(String interCode, String interStatut) {
    Ticket ticket = ticketRepository.findByInterCode(interCode)
    		.orElseThrow();

    if (interStatut != null) {
        ticket.setInterStatut(interStatut);
    }
    // Vous pouvez ajouter d'autres champs à mettre à jour sélectivement ici

    return toTicketDTO(ticketRepository.save(ticket));
}


*/
@Override
public void deleteTicket(Ticket inter) {
	ticketRepository.delete(inter);
}


@Override
public void deleteTicketByCode(String code) {
	ticketRepository.deleteById(code);
}


@Override
public TicketDTO getTicket(String code) {
	return toTicketDTO(ticketRepository.findById(code).get());
}


@Override
public List<TicketDTO> getAllTickets() {
return ticketRepository.findAll().stream()
		.map(this::toTicketDTO)
		.collect(Collectors.toList());
}



@Override
public List<Ticket> findByInterDesignation(String desing)
{
return ticketRepository.findByInterDesignation(desing);
}
@Override
public List<Ticket> findByInterDesignationContains(String desing)
{
return ticketRepository.findByInterDesignationContains(desing);
}




@Override
public List<Ticket> findByEquipementEqptCode(String eqptCode)
{
return ticketRepository.findByEquipementEqptCode( eqptCode);}


@Override
public List<Ticket> findByInterventionNatureCode(long code)
{
return ticketRepository.findByInterventionNatureCode( code);

}


/*
@Override
public List<Ticket> findByTechnicienCodeTechnicien(long codeTechnicien)
{
return ticketRepository.findByTechnicienCodeTechnicien( codeTechnicien);

}
*/@Override
public List<Ticket> findByLoggedInTechnicien() {
    // Obtenir l'utilisateur connecté
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Vérifier si l'utilisateur est authentifié et est un utilisateur avec les rôles appropriés
    if (authentication != null && authentication.isAuthenticated()) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Récupérer les détails du technicien à partir de userDetails
        String username = userDetails.getUsername();
        Technicien technicien = technicienService.getTechnicienByUsername(username);

        // Vérifier si le technicien existe
        if (technicien != null) {
            // Obtenez la date actuelle
            LocalDate currentDate = LocalDate.now();

            // Calculez la date limite pour les tickets non archivés (date actuelle - 2 mois)
            LocalDate limitDate = currentDate.minusMonths(2);

            // Convertissez la limite de date en objet Date
            Date limitDateAsDate = Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Récupérer tous les tickets attribués à ce technicien avec la date de clôture non archivée
            List<Ticket> tickets = ticketRepository.findAllByTechnicienCodeTechnicien(technicien.getCodeTechnicien(), limitDateAsDate);

            // Filtrer les tickets avec le statut "réalisé" ou "à réaliser"
            List<Ticket> filteredTickets = tickets.stream()
                    .filter(ticket -> ticket.getInterStatut().equals("Réalisé") || ticket.getInterStatut().equals("A réaliser"))
                    .collect(Collectors.toList());

            return filteredTickets;
        }
    }
    // Si aucune condition n'est remplie ou si le technicien n'existe pas, retourner une liste vide
    return new ArrayList<>();
}


@Override
public TicketDTO updateTicketStatus(String interCode, String interStatut) {
    Optional<Ticket> optionalTicket = ticketRepository.findByInterCode(interCode);

    if (optionalTicket.isPresent()) {
        Ticket existingTicket = optionalTicket.get();
        existingTicket.setInterStatut(interStatut);
        Ticket updatedTicket = ticketRepository.save(existingTicket);
        return toTicketDTO(updatedTicket);
    } else {
        // Gérer le cas où le ticket n'existe pas
        return null; // Ou lancez une exception appropriée
    }
}

@Override
public List<Ticket> findByLoggedInDemandeur() {
    // Obtenir l'utilisateur connecté
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Vérifier si l'utilisateur est authentifié et est un utilisateur avec les rôles appropriés
    if (authentication != null && authentication.isAuthenticated()) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Récupérer les détails du demandeur à partir de userDetails
        String username = userDetails.getUsername(); // Supposons que le nom d'utilisateur soit l'identifiant du demandeur
        Demandeur demandeur = demandeurService.getDemandeurByUsername(username); // Supposez que vous avez une méthode dans le service pour rechercher le demandeur par nom d'utilisateur

        // Vérifier si le demandeur existe
        if (demandeur != null) {
            // Obtenez la date actuelle
            LocalDate currentDate = LocalDate.now();

            // Calculez la date limite pour les tickets récents (date actuelle - 2 mois)
            LocalDate limitDate = currentDate.minusMonths(2);

            // Convertissez la limite de date en objet Date
            Date limitDateAsDate = Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Récupérez tous les tickets récents associés à ce demandeur
            return ticketRepository.findAllByDemandeurCodeDemandeur(demandeur.getCodeDemandeur(), limitDateAsDate);
        }
    }
    // Si aucune condition n'est remplie ou si le demandeur n'existe pas, retourner une liste vide
    return new ArrayList<>();
}

	
	public Ticket toTicket(TicketDTO request) {
		String interCode = "I24-" + generateShortUUID().substring(0, 4); // Tronquer pour garantir que la longueur totale ne dépasse pas 10 caractères

		// Créer une nouvelle instance d'intervention
	    Intervention intervention = new Intervention();
		// Utilisation de interCode dans le reste du code
	    return Ticket.builder()
	            .interCode(interCode)
	            .intervention(intervention)
	            .interDesignation(request.getInterDesignation())
	            .interPriorite(request.getInterPriorite())
	            .interStatut(request.getInterStatut())
	            .machineArret(request.getMachineArret())
	            .dateArret(request.getDateArret())
	            .dureeArret(request.getDureeArret())
	            .dateCreation(request.getDateCreation())
	            .datePrevue(request.getDatePrevue())
	            .description(request.getDescription())
	            .sousContrat(request.getSousContrat())
	            .sousGarantie(request.getSousGarantie())
	           
	           //.intervention(request.getIntervention())
	            .equipement(request.getEquipement())
	            .demandeur(request.getDemandeur())
	            .technicien(request.getTechnicien())
	            .interventionNature(request.getInterventionNature())
	            // Map other fields if needed
	            .build();
	}

	public TicketDTO toTicketDTO(Ticket request) {
	    TicketDTO.TicketDTOBuilder builder = TicketDTO.builder()
	          
	    		.interCode(request.getInterCode())
	            .interDesignation(request.getInterDesignation())
	            .interPriorite(request.getInterPriorite())
	            .interStatut(request.getInterStatut())
	            .machineArret(request.getMachineArret())
	            .dateArret(request.getDateArret())
	            .dureeArret(request.getDureeArret())
	            .dateCreation(request.getDateCreation())
	            .datePrevue(request.getDatePrevue())
	            .description(request.getDescription())
	            .sousContrat(request.getSousContrat())
	            .sousGarantie(request.getSousGarantie())
	           
	            .intervention(request.getIntervention())
	            .equipement(request.getEquipement())
	            .demandeur(request.getDemandeur())
	            .technicien(request.getTechnicien())
	            .interventionNature(request.getInterventionNature());
	    // Map other fields if needed
	            
	    return builder.build();
	}
	

	    @Override
	    public Long countTotalTickets() {
	        return ticketRepository.count();
	    }

	    @Override
	    public Long countPendingTickets() {
	        return ticketRepository.countByInterStatut("en attente");
	    }

	    @Override
	    public Long countTodoTickets() {
	        return ticketRepository.countByInterStatut("à réaliser");
	    }

	    @Override
	    public Long countDoneTickets() {
	        return ticketRepository.countByInterStatut("réalisé");
	    }

	    @Override
	    public Long countCancelledTickets() {
	        return ticketRepository.countByInterStatut("annulé");
	    }

	    @Override
	    public Long countBlockedTickets() {
	        return ticketRepository.countByInterStatut("bloqué");
	    }
/*
	    @Override
	    public List<TicketDTO> getPlannedTickets() {
	        Date currentDate = new Date();
	        return ticketRepository.findByDatePrevueAfter(currentDate).stream()
	                .map(ticket -> {
	                    TicketDTO ticketDTO = toTicketDTO(ticket);
	                    ticketDTO.setInterCode(ticket.getInterCode());
	                    ticketDTO.setInterDesignation(ticket.getInterDesignation());
	                    ticketDTO.setDatePrevue(ticket.getDatePrevue());
	                    ticketDTO.setDureePrevue(ticket.getDureePrevue());
	                    return ticketDTO;
	                })
	                .collect(Collectors.toList());
	    }
*/
	    
	    public List<Ticket> findTicketsByDatePrevue(Date datePrevue) {
	        // Convertir Date en LocalDateTime
	        LocalDateTime startDateTime = datePrevue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	        LocalDateTime endDateTime = startDateTime.plusDays(1);

	        // Convertir LocalDateTime en Date
	        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
	        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

	        return ticketRepository.findByDatePrevueGreaterThanEqualAndDatePrevueLessThanEqual(startDate, endDate);
	    }

	    @Scheduled(fixedRate = 60000) // Exécution toutes les minutes
	    public void addNewTicketsToCalendar() {
	        Date currentDate = new Date();
	        List<Ticket> newTickets = findTicketsByDatePrevue(currentDate);
	        calendarService.addNewTicketsToCalendar(newTickets);
	    }

	  

	    
	    @Override
	    public List<TicketDTO> getClosedTickets() {
	    	
	    	// Obtenir l'utilisateur connecté
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	        // Vérifier si l'utilisateur est authentifié et est un utilisateur avec les rôles appropriés
	        if (authentication != null && authentication.isAuthenticated()) {
	            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

	            // Récupérer les détails du demandeur à partir de userDetails
	            String username = userDetails.getUsername(); // Supposons que le nom d'utilisateur soit l'identifiant du demandeur
	            Demandeur demandeur = demandeurService.getDemandeurByUsername(username); // Supposez que vous avez une méthode dans le service pour rechercher le demandeur par nom d'utilisateur
	
	         // Vérifier si le demandeur existe
	            if (demandeur != null) {
	    	
	    	
	        // Obtenez la date actuelle
	        LocalDate currentDate = LocalDate.now();

	        // Calculez la date limite pour les tickets clôturés (date de clôture + 2 mois)
	        LocalDate limitDate = currentDate.minusMonths(2);

	        // Convertissez la limite de date en objet Date
	        Date limitDateAsDate = Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

	        // Récupérez tous les tickets avec une intervention clôturée avant la limite de date
	        List<Ticket> closedTickets = ticketRepository.findAllByDemandeurCodeDemandeurAndInterventionDateClotureBefore(demandeur.getCodeDemandeur(),limitDateAsDate);

	        // Convertissez les tickets en DTO
	        List<TicketDTO> closedTicketsDTO = closedTickets.stream()
	                .map(this::toTicketDTO)
	                .collect(Collectors.toList());

	        return closedTicketsDTO;
	    }

	    
	    
	

	        }
			return null;
	        
	    }

	    @Override
	    public List<TicketDTO> getArchivedTickets() {
	        // Obtenir l'utilisateur connecté
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	        // Vérifier si l'utilisateur est authentifié et est un utilisateur avec les rôles appropriés
	        if (authentication != null && authentication.isAuthenticated()) {
	            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

	            // Récupérer les détails du technicien à partir de userDetails
	            String username = userDetails.getUsername();
	            Technicien technicien = technicienService.getTechnicienByUsername(username);

	            // Vérifier si le technicien existe
	            if (technicien != null) {
	                // Obtenez la date actuelle
	                LocalDate currentDate = LocalDate.now();

	                // Calculez la date limite pour les tickets clôturés (date de clôture + 2 mois)
	                LocalDate limitDate = currentDate.minusMonths(2);

	                // Convertissez la limite de date en objet Date
	                Date limitDateAsDate = Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

	                // Récupérez tous les tickets clôturés dont la date de clôture de l'intervention a dépassé la limite de date
	                List<Ticket> closedTickets = ticketRepository.findAllByTechnicienCodeTechnicienAndInterventionDateClotureBefore(technicien.getCodeTechnicien(), limitDateAsDate);

	                // Convertissez les tickets en DTO
	                List<TicketDTO> closedTicketsDTO = closedTickets.stream()
	                        .map(this::toTicketDTO)
	                        .collect(Collectors.toList());

	                return closedTicketsDTO;
	            }
	        }
	        // Si aucune condition n'est remplie ou si le technicien n'existe pas, retourner une liste vide
	        return new ArrayList<>();
	    }

	    
	    @Override
	    public List<Ticket> getALLArchivedTickets() {
	        // Obtenez la date actuelle
	        LocalDate currentDate = LocalDate.now();

	        // Calculez la date limite pour les tickets archivés (date de clôture + 2 mois)
	        LocalDate limitDate = currentDate.minusMonths(2);

	        // Convertissez la limite de date en objet Date
	        Date limitDateAsDate = Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

	        // Récupérez tous les tickets archivés dont la date de clôture de l'intervention a dépassé la limite de date
	        List<Ticket> archivedTickets = ticketRepository.findAllArchivedByInterventionDateClotureBefore(limitDateAsDate);

	        return archivedTickets;
	    }
  
	    
	    
	    @Override
	    public List<Ticket> getAllTickets1() {
	        // Obtenez la date actuelle
	        LocalDate currentDate = LocalDate.now();

	        // Calculez la date limite pour les tickets non archivés (date de clôture + 2 mois)
	        LocalDate limitDate = currentDate.minusMonths(2);

	        // Convertissez la limite de date en objet Date
	        Date limitDateAsDate = Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

	        // Récupérez tous les tickets qui ne sont pas archivés et dont la date de clôture de l'intervention est après la limite de date
	        List<Ticket> activeTickets = ticketRepository.findAllByInterventionDateClotureAfterOrEqualTo(limitDateAsDate);

	        return activeTickets;
	    }

}
package com.rihab.interventions.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.rihab.interventions.dto.UserDTO;
import com.rihab.interventions.entities.AuthenticationResponse;
import com.rihab.interventions.entities.Client;
import com.rihab.interventions.entities.Demandeur;
import com.rihab.interventions.entities.Departement;
import com.rihab.interventions.entities.Manager;
import com.rihab.interventions.entities.Role;
import com.rihab.interventions.entities.Technicien;
import com.rihab.interventions.entities.User;
import com.rihab.interventions.repos.DemandeurRepository;
import com.rihab.interventions.repos.ManagerRepository;
import com.rihab.interventions.repos.TechnicienRepository;
import com.rihab.interventions.repos.UserRepository;
import com.rihab.interventions.util.EmailSender;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailSender emailSender;
    private final UserDetailsService userDetailsService;
    private final DemandeurRepository demandeurRepository;
    private final TechnicienRepository technicienRepository;
    //private final TokenRepository tokenRepository;
    private final ManagerRepository managerRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(UserDTO request) {
        try {
            // Vérifier si l'utilisateur existe déjà
            if (repository.findByUsername(request.getUsername()).isPresent()) {
                return new AuthenticationResponse(null, "L'utilisateur existe déjà", null);
            }

            // Générer un nom d'utilisateur automatiquement
            String login = "user" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

            // Générer un mot de passe sécurisé
            String password = generateSecurePassword();
            
            User user = new User();
            user.setNom(request.getNom());
            user.setPrenom(request.getPrenom());
            user.setUsername(login); // Utiliser le nom d'utilisateur généré automatiquement
            user.setPassword(passwordEncoder.encode(password)); // Encodage du mot de passe sécurisé
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());

            // Enregistrer l'utilisateur dans la base de données
            user = repository.save(user);
           
            // Récupérer le rôle de l'utilisateur
            Role role = user.getRole();
            System.out.println("role"+role);
            	 if(role.equals(Role.CLIENT)) {
            		 Demandeur demandeur=new Demandeur();
            		 demandeur.setPost(request.getPost());
            		 demandeur.setUser(user);
            		 demandeur.setClient(Client.builder().codeClient(request.getCodeClient()).build());
            		 demandeurRepository.save(demandeur);
            		 user.setDemandeur(demandeur);
            		  repository.save(user);
            	 }
            	 else if(role.equals(Role.TECHNICIEN)) {
            		 Technicien technicien=new Technicien();
            		
            		 technicien.setMatricule(request.getMatricule());
            		 technicien.setResponsable(request.getResponsable());
            		 technicien.setInternet(request.getInternet());
            		 technicien.setNumeroAbrege(request.getNumeroAbrege());
            		
            		 technicien.setUser(user);
            		 technicien.setDepartement(Departement.builder().codeDepart(request.getCodeDepart()).build());
            		
					technicienRepository.save(technicien);
            		 user.setTechnicien(technicien);
            		  repository.save(user);
            	 }
            	 
            	 
            	 else if(role.equals(Role.MANAGER)) {
            		 Manager manager=new Manager();
            		
            		 manager.setUser(user);
            		
            		
					managerRepository.save(manager);
            		 user.setManager(manager);
            		  repository.save(user);
            	 }

            	 
            	 
            // Générer le token JWT en tenant compte du rôle de l'utilisateur
            String jwt = jwtService.generateTokenWithRole(user, role);

            // Construire le contenu de l'e-mail
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("Bonjour ").append(user.getNom()).append(" ").append(user.getPrenom()).append(",\n\n");
            emailContent.append("Votre compte a été créé avec succès.\n");
            emailContent.append("Voici vos informations d'inscription :\n");
            emailContent.append("Nom d'utilisateur : ").append(login).append("\n");
            emailContent.append("Mot de passe : ").append(password).append("\n\n");
            emailContent.append("Merci de votre inscription.");

            // Envoyer l'e-mail
            emailSender.sendEmail(user.getEmail(), emailContent.toString());
     
            return new AuthenticationResponse(jwt, "Inscription de l'utilisateur réussie", role);
              
        } catch (Exception e) {
            return new AuthenticationResponse(null, "Erreur lors de l'enregistrement de l'utilisateur : " + e.getMessage(), null);
        } 
    }


    
    private String generateSecurePassword() {
	// TODO Auto-generated method stub
    	 return RandomStringUtils.randomAlphanumeric(10);
}/*
    public AuthenticationResponse authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password
                    )
            );

            User user = repository.findByUsername(username).orElseThrow();
            String jwt = jwtService.generateToken(user);

            return new AuthenticationResponse(jwt, "User login was successful");
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }
 
*/
   /* 
    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        String jwt = jwtService.generateToken(user);

      
        return new AuthenticationResponse(jwt, "User login was successful");

    }
    */
    
    public AuthenticationResponse authenticate(UserDTO request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            // Récupérer les détails de l'utilisateur après l'authentification
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            // Récupérer le rôle de l'utilisateur
            Role role = ((User) userDetails).getRole();

            // Générer le token JWT en tenant compte du rôle de l'utilisateur
            String jwt = jwtService.generateTokenWithRole(userDetails, role);

            // Utiliser le constructeur mis à jour avec le rôle
            return new AuthenticationResponse(jwt, "User login was successful", role);
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }


    
    
    
    
    
    
    /*
	public AuthenticationResponse authenticate(String login, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            login,
                            password
                    )
            );

            User user = repository.findByUsername(login).orElseThrow();
            String jwt = jwtService.generateToken(user);

            return new AuthenticationResponse(jwt, "User login was successful");
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }
    */
    
    
	/*
    public AuthenticationResponse register(User request) {

        // check if user already exist. if exist than authenticate the user
        if(repository.findByUsername(request.getUsername()).isPresent()) {
            return new AuthenticationResponse(null, "User already exist");
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setUsername(UUID.randomUUID().toString());
      
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEmail(request.getEmail());

        user.setRole(request.getRole());

        user = repository.save(user);
        // Envoyer les informations par e-mail
       // envoyerEmail(user);
        
        String jwt = jwtService.generateToken(user);

       // saveUserToken(jwt, user);

        return new AuthenticationResponse(jwt, "User registration was successful");

    }

  */
    /*// hedhya loula mt3 email:
    public AuthenticationResponse register(User request) {

        // Vérifier si l'utilisateur existe déjà
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            return new AuthenticationResponse(null, "L'utilisateur existe déjà");
        }

        // Générer un nom d'utilisateur automatiquement
        String login = "user" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        // Générer un mot de passe sécurisé
        String password = generateSecurePassword();

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setUsername(login); // Utiliser le nom d'utilisateur généré automatiquement
        user.setPassword(passwordEncoder.encode(password)); // Encodage du mot de passe sécurisé
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        // Enregistrer l'utilisateur dans la base de données
        try {
            user = repository.save(user);
        } catch (Exception e) {
            return new AuthenticationResponse(null, "Erreur lors de l'enregistrement de l'utilisateur");
        }

        // Construire le contenu de l'e-mail
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Bonjour ").append(user.getNom()).append(" ").append(user.getPrenom()).append(",\n\n");
        emailContent.append("Votre compte a été créé avec succès.\n");
        emailContent.append("Voici vos informations d'inscription :\n");
        emailContent.append("Nom d'utilisateur : ").append(login).append("\n");
        emailContent.append("Mot de passe : ").append(password).append("\n\n");
        emailContent.append("Merci de votre inscription.");

        // Envoyer l'e-mail
        emailSender.sendEmail(user.getEmail(), emailContent.toString());

        // Générer le token JWT
        String jwt = jwtService.generateToken(user);

        return new AuthenticationResponse(jwt, "Inscription de l'utilisateur réussie");
    }
/*
    // Méthode pour générer un mot de passe sécurisé
    private String generateSecurePassword() {
        // Utilisez une bibliothèque pour générer un mot de passe sécurisé, par exemple, RandomStringUtils de Apache Commons
        // Ici, je vais utiliser une chaîne aléatoire simple pour illustrer.
        return RandomStringUtils.randomAlphanumeric(10); // Génère une chaîne alphanumérique de 10 caractères
    }

  /*  
    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        String jwt = jwtService.generateToken(user);

      //  revokeAllTokenByUser(user);
        //saveUserToken(jwt, user);

        return new AuthenticationResponse(jwt, "User login was successful");

    }*/
    
    
    /*
    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllTokensByUser(user.getId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }
    */
    /*
    private void saveUserToken(String jwt, User user) {
        Token token = new Token();
        token.setToken(jwt);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }*/
    
  
 // Importez les classes nécessaires

    public List<UserDTO> getAllUsers() {
        List<User> users = repository.findAll();
        List<UserDTO> userDTOs = new ArrayList<>();

        for (User user : users) {
            UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .tel(user.getTel())
                .age(user.getAge())
                .role(user.getRole())
                .sexe(user.getSexe())
                .dateEmbauche(user.getDateEmbauche())
                .build();
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }

    // Méthode pour supprimer un utilisateur par son identifiant
    @Transactional
    public void deleteUser(Long userId) {
        repository.deleteById(userId);
    }
    
    
    
    public List<UserDTO> getAllManagers() {
        List<User> managers = repository.findByRole(Role.MANAGER); // Utilisation de l'énumération Role.MANAGER pour rechercher les utilisateurs avec le rôle de manager
        List<UserDTO> managerDTOs = new ArrayList<>();

        for (User manager : managers) {
            UserDTO managerDTO = UserDTO.builder()
                .id(manager.getId())
                .nom(manager.getNom())
                .prenom(manager.getPrenom())
                .email(manager.getEmail())
                .tel(manager.getTel())
                .age(manager.getAge())
                .role(manager.getRole())
                .sexe(manager.getSexe())
                .dateEmbauche(manager.getDateEmbauche())
                .build();
            managerDTOs.add(managerDTO);
        }
        return managerDTOs;
    }

  
    public UserDTO updateProfile(UserDTO userDTO) {
        // Récupérer l'utilisateur actuellement authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Mettre à jour les informations générales de l'utilisateur
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setTel(userDTO.getTel());
        user.setAge(userDTO.getAge());
        user.setUsername(userDTO.getUsername());
user.setDateEmbauche(userDTO.getDateEmbauche());
        // Vérifier le rôle de l'utilisateur
        if (user.getRole() == Role.TECHNICIEN) {
            // Récupérer l'entité Technicien associée à l'utilisateur
            Technicien technicien = user.getTechnicien();

            // Vérifier si l'entité Technicien existe
            if (technicien != null) {
                // Mettre à jour les champs spécifiques pour un technicien
                
               
                technicien.setResponsable(userDTO.getResponsable());

                // Enregistrer les modifications de l'entité Technicien
                technicienRepository.save(technicien);
            } else {
                // Gérer le cas où l'entité Technicien n'existe pas
                throw new RuntimeException("L'entité Technicien n'existe pas pour cet utilisateur");
            }
        } else if (user.getRole() == Role.MANAGER) {
            // Récupérer l'entité Manager associée à l'utilisateur
            Manager manager = user.getManager();

            // Vérifier si l'entité Manager existe
            if (manager != null) {
                // Mettre à jour les champs spécifiques pour un manager
             
                // Enregistrer les modifications de l'entité Manager
                managerRepository.save(manager);
            } else {
                // Gérer le cas où l'entité Manager n'existe pas
                throw new RuntimeException("L'entité Manager n'existe pas pour cet utilisateur");
            }
        } else if (user.getRole() == Role.CLIENT) {
            // Récupérer l'entité Demandeur associée à l'utilisateur
            Demandeur demandeur = user.getDemandeur();

            // Vérifier si l'entité Demandeur existe
            if (demandeur != null) {
                // Mettre à jour les champs spécifiques pour un demandeur
                demandeur.setPost(userDTO.getPost());

                // Enregistrer les modifications de l'entité Demandeur
                demandeurRepository.save(demandeur);
            } else {
                // Gérer le cas où l'entité Demandeur n'existe pas
                throw new RuntimeException("L'entité Demandeur n'existe pas pour cet utilisateur");
            }
        }

        // Si le mot de passe a été modifié, l'encoder avant de le mettre à jour en base
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Enregistrer les modifications de l'utilisateur
        User savedUser = repository.save(user);

        // Construire le UserDTO à retourner
        UserDTO updatedUser = new UserDTO();
        updatedUser.setUsername(savedUser.getUsername());
        updatedUser.setNom(savedUser.getNom());
        updatedUser.setPrenom(savedUser.getPrenom());
        updatedUser.setEmail(savedUser.getEmail());
        updatedUser.setTel(savedUser.getTel());
        updatedUser.setDateEmbauche(savedUser.getDateEmbauche());
        updatedUser.setRole(savedUser.getRole());

        return updatedUser;
    }

    
    
    
    // ...
    public UserDTO getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDTO currentUser = new UserDTO();
        currentUser.setUsername(user.getUsername());
              // Vérifie le rôle de l'utilisateur
        if (user.getRole() == Role.CLIENT) {
            currentUser.setNom(user.getNom());
            currentUser.setPrenom(user.getPrenom());
            currentUser.setEmail(user.getEmail());
            currentUser.setTel(user.getTel());
            currentUser.setAge(user.getAge());
            currentUser.setPost(user.getDemandeur().getPost());
        } else if (user.getRole() == Role.TECHNICIEN) {
            currentUser.setNom(user.getNom());
            currentUser.setPrenom(user.getPrenom());
            currentUser.setEmail(user.getEmail());
            currentUser.setTel(user.getTel());
            currentUser.setAge(user.getAge());
          
            currentUser.setResponsable(user.getTechnicien().getResponsable());
            currentUser.setDateEmbauche(user.getDateEmbauche());
            currentUser.setNomdepart(user.getTechnicien().getDepartement().getNomDepart());
        } else if (user.getRole() == Role.MANAGER) {
            currentUser.setNom(user.getNom());
            currentUser.setPrenom(user.getPrenom());
            currentUser.setEmail(user.getEmail());
            currentUser.setTel(user.getTel());
            currentUser.setAge(user.getAge());
         
            currentUser.setDateEmbauche(user.getDateEmbauche());
        }
        // Ajoutez d'autres conditions pour d'autres rôles si nécessaire

        return currentUser;
    }


    
    
}

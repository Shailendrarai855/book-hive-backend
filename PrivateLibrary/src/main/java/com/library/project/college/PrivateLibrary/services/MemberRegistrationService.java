package com.library.project.college.PrivateLibrary.services;

import com.library.project.college.PrivateLibrary.dto.MemberDTO;
import com.library.project.college.PrivateLibrary.dto.MemberRegistrationDTO;
import com.library.project.college.PrivateLibrary.dto.ResponseDTO;
import com.library.project.college.PrivateLibrary.entities.AdminEntity;
import com.library.project.college.PrivateLibrary.entities.MemberEntity;
import com.library.project.college.PrivateLibrary.entities.MemberRegistrationEntity;
import com.library.project.college.PrivateLibrary.exceptions.ResourceNotFoundException;
import com.library.project.college.PrivateLibrary.repository.AdminRepository;
import com.library.project.college.PrivateLibrary.repository.MemberRegistrationRepository;
import com.library.project.college.PrivateLibrary.repository.MemberRepository;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class MemberRegistrationService {
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final MemberRegistrationRepository memberRegistrationRepository;
    private final AdminRepository adminRepository;
    private  final  EmailService emailService;

    public MemberRegistrationService(ModelMapper modelMapper, MemberRepository memberRepository, MemberRegistrationRepository memberRegistrationRepository, AdminRepository adminRepository, EmailService emailService) {
        this.modelMapper = modelMapper;
        this.memberRepository = memberRepository;
        this.memberRegistrationRepository = memberRegistrationRepository;
        this.adminRepository = adminRepository;
        this.emailService = emailService;
    }

    // Convert MemberRegistrationEntity to DTO
    public MemberRegistrationDTO convertToDTO(MemberRegistrationEntity memberRegistrationEntity) {
        return modelMapper.map(memberRegistrationEntity, MemberRegistrationDTO.class);
    }

    public MemberDTO convertToMemberDTO(MemberRegistrationEntity memberRegistrationEntity){

            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setName(memberRegistrationEntity.getName());
            memberDTO.setGender(memberRegistrationEntity.getGender());
            memberDTO.setEmail(memberRegistrationEntity.getEmail());
            memberDTO.setAddress(memberRegistrationEntity.getAddress());
            memberDTO.setContactNo(memberRegistrationEntity.getContactNo());
            memberDTO.setPassword(memberRegistrationEntity.getPassword());
            return memberDTO;
    }

    // Get all member registration requests
    public List<MemberRegistrationDTO> getAllRequests() {
        List<MemberRegistrationEntity> allRequests = memberRegistrationRepository.findAll();
        return allRequests.stream()
                .map(this::convertToDTO)
                .toList();
    }


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }

    // Create a new member registration request
    public MemberRegistrationDTO createNewMember(MemberRegistrationDTO memberRegistrationDTO) {
        System.out.println( "3");
        if (memberRegistrationRepository.existsByEmail(memberRegistrationDTO.getEmail())) {
            throw new ResourceNotFoundException("Request Pending. Can't Create New Request");
        }
        System.out.println( "controller 4");
        if (memberRepository.existsByEmail(memberRegistrationDTO.getEmail())) {
            throw new ResourceNotFoundException("Already a member. Please Sign In.");
        }
        System.out.println( "controller5");
        MemberRegistrationEntity savedRequest = memberRegistrationRepository.save(modelMapper.map(memberRegistrationDTO, MemberRegistrationEntity.class));
        return convertToDTO(savedRequest);
    }


    public ResponseDTO approvedMemberRequests(String registrationId, String adminId) {

        MemberRegistrationEntity requestMember = memberRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration ID not found: "+ registrationId));

        AdminEntity currentAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin ID not found: " +adminId));
        MemberDTO memberDTO = convertToMemberDTO(requestMember);

        memberDTO.setAdminId(adminId);
        memberDTO.setPassword(generatePassword(6));
        memberDTO.setIsActive(true);
        MemberEntity newMember = modelMapper.map(memberDTO,MemberEntity.class);
        memberRepository.save(newMember);
        currentAdmin.getMembers().add(newMember);
        memberRegistrationRepository.deleteById(registrationId);

        String memberName = newMember.getName();
        String to =  newMember.getEmail();
        String  memberId = newMember.getMemberId();
        String password = newMember.getPassword();
        String subject = "Welcome to our Library Your Membership Details";
        String body =
                "Dear " + memberName + ",\n\n" +
                        "Welcome to Our Library! Thank you for joining our community.\n\n" +
                        "Your membership has been activated. Here are your details:\n\n" +
                        "Member ID: " + memberId + "\n" +
                        "Password: " + password + "\n\n" +
                        "If you have any questions, feel free to contact us.\n\n" +
                        "Best regards,\n" +
                        "Prashant Verma\n" +
                        "Library Management Team";



        emailService.sendMail(to,subject,body);
        return ResponseDTO.builder()
                .message("Member created")
                .build();
    }

    // Reject a member registration request
    public ResponseDTO  rejectMemberRequest(String registrationId) {
            MemberRegistrationEntity member = memberRegistrationRepository.findById(registrationId).get();
            String to = member.getEmail();
            String userName = member.getName();
            String subject = "Library Membership Request Rejected";
            String body = "Dear " + userName + ",\n\n" +
                    "We regret to inform you that we are unable to approve your request to become a member of our library at this time. Unfortunately, certain unforeseen circumstances prevent us from proceeding with your membership.\n\n" +
                    "Membership Details:\n" +
                    "Registration ID: " + registrationId + "\n\n" +
                    "If you have any questions or would like further assistance, please feel free to reach out to us. We apologize for any inconvenience this may have caused.\n\n" +
                    "Thank you for your interest in our library and for your understanding.\n\n" +
                    "Best regards,\n" +
                    "Library Management Team";
        memberRegistrationRepository.deleteById(registrationId);
        System.out.println("hjjbjbf");
        emailService.sendMail(to,subject,body);
        return  ResponseDTO.builder()
                .message("request deleted")
                .build();
    }
}

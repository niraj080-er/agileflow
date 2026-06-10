package com.swiggy.agileflow.project.application;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.project.api.AddProjectMemberRequest;
import com.swiggy.agileflow.project.api.CreateProjectRequest;
import com.swiggy.agileflow.project.api.ProjectMemberResponse;
import com.swiggy.agileflow.project.api.ProjectResponse;
import com.swiggy.agileflow.project.domain.Project;
import com.swiggy.agileflow.project.domain.ProjectMember;
import com.swiggy.agileflow.project.infrastructure.ProjectMemberRepository;
import com.swiggy.agileflow.project.infrastructure.ProjectRepository;
import com.swiggy.agileflow.user.infrastructure.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest req) {
        if (projectRepository.findByProjectKey(req.projectKey()).isPresent()) {
            throw new BusinessRuleException("Project key already exists: " + req.projectKey());
        }
        if (req.leadUserId() != null && !userRepository.existsById(req.leadUserId())) {
            throw NotFoundException.of("User", req.leadUserId());
        }
        Project project = new Project();
        project.setProjectKey(req.projectKey());
        project.setName(req.name());
        project.setDescription(req.description());
        project.setLeadUserId(req.leadUserId());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) {
        return ProjectResponse.from(projectRepository.findById(id)
            .orElseThrow(() -> NotFoundException.of("Project", id)));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        return projectRepository.findAll().stream().map(ProjectResponse::from).toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(Long projectId, AddProjectMemberRequest req) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        if (!userRepository.existsById(req.userId())) {
            throw NotFoundException.of("User", req.userId());
        }
        boolean alreadyMember = memberRepository.findByProjectId(projectId).stream()
            .anyMatch(m -> m.getUserId().equals(req.userId()));
        if (alreadyMember) {
            throw new BusinessRuleException("User " + req.userId() + " is already a member of this project.");
        }
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(req.userId());
        member.setRole(req.role() != null ? req.role() : "MEMBER");
        return ProjectMemberResponse.from(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        return memberRepository.findByProjectId(projectId).stream()
            .map(ProjectMemberResponse::from).toList();
    }
}

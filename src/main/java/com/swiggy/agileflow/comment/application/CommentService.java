package com.swiggy.agileflow.comment.application;

import com.swiggy.agileflow.activity.application.ActivityLogPort;
import com.swiggy.agileflow.activity.domain.ActivityActionType;
import com.swiggy.agileflow.comment.api.AddCommentRequest;
import com.swiggy.agileflow.comment.api.CommentResponse;
import com.swiggy.agileflow.comment.domain.Comment;
import com.swiggy.agileflow.comment.infrastructure.CommentRepository;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.issue.domain.Issue;
import com.swiggy.agileflow.issue.infrastructure.IssueRepository;
import com.swiggy.agileflow.notification.application.NotificationPort;
import com.swiggy.agileflow.notification.domain.NotificationType;
import com.swiggy.agileflow.realtime.application.RealtimeBroadcaster;
import com.swiggy.agileflow.realtime.domain.RealtimeEventType;
import com.swiggy.agileflow.user.infrastructure.UserRepository;
import com.swiggy.agileflow.watcher.infrastructure.WatcherRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final WatcherRepository watcherRepository;
    private final ActivityLogPort activityLog;
    private final NotificationPort notificationPort;
    private final RealtimeBroadcaster broadcaster;

    public CommentService(CommentRepository commentRepository,
                          IssueRepository issueRepository,
                          UserRepository userRepository,
                          WatcherRepository watcherRepository,
                          ActivityLogPort activityLog,
                          NotificationPort notificationPort,
                          RealtimeBroadcaster broadcaster) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.watcherRepository = watcherRepository;
        this.activityLog = activityLog;
        this.notificationPort = notificationPort;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public CommentResponse addComment(Long issueId, AddCommentRequest req) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> NotFoundException.of("Issue", issueId));
        if (!userRepository.existsById(req.authorId())) {
            throw NotFoundException.of("Author", req.authorId());
        }

        Comment comment = new Comment();
        comment.setIssueId(issueId);
        comment.setAuthorId(req.authorId());
        comment.setBody(req.body());
        Comment saved = commentRepository.save(comment);

        activityLog.record(issue.getProjectId(), issueId, req.authorId(),
            ActivityActionType.COMMENT_ADDED, null, null, saved.getId().toString());

        String payload = saved.getBody().length() > 200
            ? saved.getBody().substring(0, 200) : saved.getBody();
        watcherRepository.findByIssueId(issueId).forEach(w ->
            notificationPort.notify(w.getUserId(), NotificationType.WATCHER_UPDATE,
                issueId, payload));

        broadcaster.broadcast(issue.getProjectId(), RealtimeEventType.COMMENT_ADDED,
            issue.getIssueKey());

        return CommentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw NotFoundException.of("Issue", issueId);
        }
        return commentRepository.findByIssueIdOrderByIdAsc(issueId).stream()
            .map(CommentResponse::from)
            .toList();
    }
}

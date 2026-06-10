package com.swiggy.agileflow.issue.domain;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Encodes the issue-type parent/child rules:
 * <ul>
 *   <li>Epic       -> top-level only (no parent).</li>
 *   <li>Story/Task/Bug -> optional Epic parent.</li>
 *   <li>Sub-task   -> must have a Story, Task, or Bug parent.</li>
 * </ul>
 * Open for extension: the allowed-parent map is the single place to evolve the rules.
 */
@Component
public class IssueHierarchyPolicy {

    /** Allowed parent types per child type. An empty set means "must be top-level". */
    private static final Map<IssueType, Set<IssueType>> ALLOWED_PARENTS = Map.of(
        IssueType.EPIC,    EnumSet.noneOf(IssueType.class),
        IssueType.STORY,   EnumSet.of(IssueType.EPIC),
        IssueType.TASK,    EnumSet.of(IssueType.EPIC),
        IssueType.BUG,     EnumSet.of(IssueType.EPIC),
        IssueType.SUBTASK, EnumSet.of(IssueType.STORY, IssueType.TASK, IssueType.BUG)
    );

    /** Child types that require a parent. */
    private static final Set<IssueType> REQUIRES_PARENT = EnumSet.of(IssueType.SUBTASK);

    /**
     * Validates a child issue's parent relationship.
     *
     * @param childType  the type of the issue being created/updated
     * @param parentType the parent's type, or {@code null} when there is no parent
     * @throws BusinessRuleException if the pairing is not allowed (maps to 422)
     */
    public void validateParent(IssueType childType, IssueType parentType) {
        Set<IssueType> allowed = ALLOWED_PARENTS.getOrDefault(childType, EnumSet.noneOf(IssueType.class));

        if (parentType == null) {
            if (REQUIRES_PARENT.contains(childType)) {
                throw new BusinessRuleException(childType + " must have a parent issue.");
            }
            return;
        }

        if (!allowed.contains(parentType)) {
            throw new BusinessRuleException(
                "A " + childType + " cannot be a child of a " + parentType + ".");
        }
    }
}

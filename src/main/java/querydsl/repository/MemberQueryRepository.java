package querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.dto.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static querydsl.entity.QMember.member;
import static querydsl.entity.QTeam.team;

@Repository
public class MemberQueryRepository {

    private final JPAQueryFactory query;

    public MemberQueryRepository(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    public List<MemberTeamDto> search(MemberSearchCondition cond) {
        return query
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamnameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}

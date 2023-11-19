package querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.dto.QMemberTeamDto;
import querydsl.entity.Member;

import java.util.List;

import static org.springframework.util.StringUtils.countOccurrencesOf;
import static org.springframework.util.StringUtils.hasText;
import static querydsl.entity.QMember.member;
import static querydsl.entity.QTeam.team;

public class MemberRepositoryCustomImpl
        //extends QuerydslRepositorySupport
        implements MemberRepositoryCustom {
    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     *
     * @param domainClass must not be {@literal null}.
     */
    /*public MemberRepositoryCustomImpl(Class<?> domainClass) {
        super(Member.class);
    }*/

    private final JPAQueryFactory query;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition cond) {

        /*return List<MemberTeamDto> result = from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                )
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .fetch();*/

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

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition cond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = query
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long totalCount = results.getTotal();

        return new PageImpl<>(content, pageable, totalCount);
    }

        /*@Override
        public Page<MemberTeamDto> searchPageSimple2(MemberSearchCondition cond, Pageable pageable) {

            QueryResults<MemberTeamDto> results = from(member)
                    .leftJoin(member.team, team)
                    .where(
                            usernameEq(cond.getUsername()),
                            teamnameEq(cond.getTeamName()),
                            ageGoe(cond.getAgeGoe()),
                            ageLoe(cond.getAgeLoe())
                    )
                    .select(new QMemberTeamDto(
                            member.id,
                            member.username,
                            member.age,
                            team.id.as("teamId"),
                            team.name.as("teamName")
                    ))
                    .fetchResults();

            //offset, limit 적용
            JPQLQuery<T> query = getQuerydsl().applyPagination(pageable, results);

            List<MemberTeamDto> content = results.getResults();
            long totalCount = results.getTotal();

            return new PageImpl<>(content, pageable, totalCount);
        }*/

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition cond, Pageable pageable) {
        List<MemberTeamDto> content = query
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //직접 Count 쿼리 실행
        JPAQuery<Long> count = query
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamnameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                );

        //return new PageImpl<>(content, pageable, count);
        /**
         * 페이지의 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
         * 마지막 페이지일 때
         *  -> countQuery 호출하지 않는다.
         */
        return PageableExecutionUtils.getPage(content, pageable,
                count::fetchCount);
    }

    @Override
    public List<Member> sort(Pageable pageable) {
        JPAQuery<Member> sortQuery =
                query
                .selectFrom(member);

        for (Sort.Order o : pageable.getSort()) {
            PathBuilder builder = new PathBuilder(member.getType(), member.getMetadata());
            sortQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC,
                    builder.get(o.getProperty())));
        }

        List<Member> result = sortQuery.fetch();

        return result;
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

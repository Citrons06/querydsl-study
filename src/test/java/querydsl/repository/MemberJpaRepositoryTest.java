package querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.entity.Member;
import querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired MemberJpaRepository repository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = repository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = repository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    void querydslTest() {
        Member member = new Member("member1", 10);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = repository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = repository.findByUsername_Querydsl("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(35);
        cond.setAgeLoe(40);
        cond.setTeamName("teamB");

        List<MemberTeamDto> result = repository.searchByBuilder(cond);

        assertThat(result).extracting("username")
                .containsExactly("member4");
    }

    @Test
    void searchTest2() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(35);
        cond.setAgeLoe(40);
        cond.setTeamName("teamB");

        List<MemberTeamDto> result = repository.search(cond);

        assertThat(result).extracting("username")
                .containsExactly("member4");
    }


}
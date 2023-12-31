package querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition cond);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition cond, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition cond, Pageable pageable);

    List<Member> sort(Pageable pageable);
}

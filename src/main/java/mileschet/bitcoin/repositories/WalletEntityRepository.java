package mileschet.bitcoin.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletEntityRepository extends PagingAndSortingRepository<WalletEntity, Long> {
    public WalletEntity findOneById(Long id);
}

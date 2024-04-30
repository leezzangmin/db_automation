package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.entity.MysqlAccount;
import zzangmin.db_automation.repository.MysqlAccountRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MysqlAccountService {

    private final MysqlAccountRepository mysqlAccountRepository;

    @Transactional
    public void upsertAccounts(List<MysqlAccount> mysqlAccounts) {
        for (MysqlAccount mysqlAccount : mysqlAccounts) {
            if (mysqlAccountRepository.findByServiceNameAndUserAndHost(mysqlAccount.getServiceName(), mysqlAccount.getUser(), mysqlAccount.getHost()).isPresent()) {
                continue;
            }
            mysqlAccountRepository.save(mysqlAccount);
        }
    }

    @Transactional(readOnly = true)
    public List<MysqlAccount> findAccounts() {
        List<MysqlAccount> mysqlAccounts = mysqlAccountRepository.findAll();
        return mysqlAccounts;
    }
}

package zzangmin.db_automation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zzangmin.db_automation.entity.MysqlAccount;
import zzangmin.db_automation.repository.MysqlAccountRepository;
import zzangmin.db_automation.repository.PrivilegeRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MysqlAccountService {

    private final MysqlAccountRepository mysqlAccountRepository;
    private final PrivilegeRepository privilegeRepository;

    // TODO: delete+insert -> update
    @Transactional
    public void upsertAccounts(List<MysqlAccount> mysqlAccounts) {
        for (MysqlAccount mysqlAccount : mysqlAccounts) {
            Optional<MysqlAccount> optionalFindMysqlAccount = mysqlAccountRepository.findByServiceNameAndUserAndHost(mysqlAccount.getServiceName(),
                    mysqlAccount.getUser(),
                    mysqlAccount.getHost());
            if (optionalFindMysqlAccount.isPresent()) {
                MysqlAccount findMysqlAccount = optionalFindMysqlAccount.get();
                privilegeRepository.deleteAll(findMysqlAccount.getPrivileges());
                findMysqlAccount.getPrivileges().clear();
                for (MysqlAccount.Privilege privilege : mysqlAccount.getPrivileges()) {
                    privilege.setMysqlAccount(findMysqlAccount);
                    findMysqlAccount.getPrivileges().add(privilege);
                }
                mysqlAccountRepository.save(findMysqlAccount);
            } else {
                mysqlAccount.getPrivileges().forEach(privilege -> privilege.setMysqlAccount(mysqlAccount));
                mysqlAccountRepository.save(mysqlAccount);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MysqlAccount> findAccounts(String serviceName) {
        return mysqlAccountRepository.findByServiceName(serviceName);
    }
}

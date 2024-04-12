package zzangmin.db_automation.service;

import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.StaticSelectElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlackService {

    private final DescribeService describeService;

    public StaticSelectElement findClusterSelects() {
        List<OptionObject> selectOptions = describeService.findDBMSNames()
                .getDbmsNames()
                .stream()
                .map(dbmsName -> OptionObject.builder()
                        .text(plainText(dbmsName))
                        .value(dbmsName)
                        .build()
                )
                .collect(Collectors.toList());
        StaticSelectElement clusterSelects = StaticSelectElement.builder()
                .options(selectOptions)
                .placeholder(plainText("select cluster"))
                .actionId("cluster")
                .build();

        return clusterSelects;
    }


}

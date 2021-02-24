package eci.arsw.covidanalyzer.service;

import eci.arsw.covidanalyzer.model.Result;
import eci.arsw.covidanalyzer.model.ResultType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service("ICovidAggregateServiceStub")
public class ICovidAggregateServiceStub implements ICovidAggregateService {
    @Override
    public boolean aggregateResult(Result result, ResultType type) {
        return false;
    }

    @Override
    public boolean getResult(ResultType type) {
        return false;
    }


    @Override
    public void upsertPersonWithMultipleTests(UUID id, ResultType type) {

    }
}

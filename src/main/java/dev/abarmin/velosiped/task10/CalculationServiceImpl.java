package dev.abarmin.velosiped.task10;

@Service
public class CalculationServiceImpl implements CalculationService {
    @Override
    public int calculate(int arg1, int arg2) {
        return arg1 + arg2;
    }
}

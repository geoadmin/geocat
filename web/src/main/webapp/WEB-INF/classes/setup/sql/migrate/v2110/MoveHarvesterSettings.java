package v2110;

import v280.MoveHarvesterSettingsToHigherNumber;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Jesse on 10/31/2014.
 */
public class MoveHarvesterSettings extends MoveHarvesterSettingsToHigherNumber {
    @Override
    public void update(Statement statement) throws SQLException {
        super.counter.set(100);
        super.update(statement);
    }

    @Override
    protected String getHarvesterSettingsName() {
        return "HarvesterSettings";
    }
}

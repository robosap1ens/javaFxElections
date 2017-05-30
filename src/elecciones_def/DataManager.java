/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elecciones_def;

import electionresults.model.ElectionResults;
import electionresults.persistence.io.DataAccessLayer;
import java.util.List;
import javafx.concurrent.Task;

/**
 *
 * @author ganimedes
 */
public class DataManager extends Task<List<ElectionResults>> {
    
    
    public DataManager(){
        
    }
    @Override
    protected List<ElectionResults> call() throws Exception {
        
        return DataAccessLayer.getAllElectionResults();
    }
    
}

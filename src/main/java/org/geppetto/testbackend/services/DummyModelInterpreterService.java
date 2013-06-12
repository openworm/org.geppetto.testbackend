package org.geppetto.testbackend.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.visualisation.model.Particle;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.core.visualisation.model.Scene;

public class DummyModelInterpreterService implements IModelInterpreter{

	private static Log logger = LogFactory.getLog(DummyModelInterpreterService.class);

	@Override
	public IModel readModel(URL url) throws ModelInterpreterException {
		ModelWrapper wrapper = new ModelWrapper(UUID.randomUUID().toString());
		return wrapper;
	}

	@Override
	public Scene getSceneFromModel(IModel arg0, StateTreeRoot arg1)
			throws ModelInterpreterException {
		// TODO Auto-generated method stub
		return new Scene();
	}

}

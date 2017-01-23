package importAutomatonFromLogOrModel;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.concurrent.TimeUnit;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import Automaton.Automaton;
import Automaton.State;
import Automaton.Transition;
import name.kazennikov.dafsa.AbstractIntDAFSA;
import name.kazennikov.dafsa.IntDAFSAInt;

public class ImportEventLog {
	private BiMap<Integer, String> labelMapping;
	private BiMap<String, Integer> inverseLabelMapping;
	private BiMap<Integer, State> stateMapping;
	private BiMap<Integer, Transition> transitionMapping;
	private IntHashSet finalStates;
	//private Map<IntArrayList, Boolean> tracesContained;
	private Map<IntArrayList, IntArrayList> caseTracesMapping;
	//private IntObjectHashMap<String> traceIDtraceName;
	
	public XLog importEventLog(String fileName) throws Exception
	{
		File xesFileIn = new File(fileName);
		XesXmlParser parser = new XesXmlParser(new XFactoryNaiveImpl());
        if (!parser.canParse(xesFileIn)) {
        	parser = new XesXmlGZIPParser();
        	if (!parser.canParse(xesFileIn)) {
        		throw new IllegalArgumentException("Unparsable log file: " + xesFileIn.getAbsolutePath());
        	}
        }
        List<XLog> xLogs = parser.parse(xesFileIn);

       return xLogs.remove(0);
	}
	
	public Automaton convertLogToAutomatonFrom(String fileName) throws Exception {
		//long start = System.nanoTime();
		File xesFileIn = new File(fileName);
		XesXmlParser parser = new XesXmlParser(new XFactoryNaiveImpl());
        if (!parser.canParse(xesFileIn)) {
        	parser = new XesXmlGZIPParser();
        	if (!parser.canParse(xesFileIn)) {
        		throw new IllegalArgumentException("Unparsable log file: " + xesFileIn.getAbsolutePath());
        	}
        }
        List<XLog> xLogs = parser.parse(xesFileIn);

        XLog xLog = xLogs.remove(0);
        /*
        while (xLogs.size() > 0) {
        	xLog.addAll(xLogs.remove(0));
        }
        */
        //long end = System.nanoTime();
        //System.out.println("Log import: " + TimeUnit.SECONDS.convert((end - start), TimeUnit.NANOSECONDS) + "s");
        return this.createDAFSAfromLog(xLog);

        //OutputStream outStream = new FileOutputStream(xesFileOut);
		//new XesXmlGZIPSerializer().serialize(xLog, outStream);
	}
	
	public Automaton createDAFSAfromLog(XLog xLog) throws IOException
	{	
		//long start = System.nanoTime();
		XConceptExtension xce = XConceptExtension.instance();
		//tracesContained = new UnifiedMap<IntArrayList, Boolean>();
		caseTracesMapping = new UnifiedMap<IntArrayList, IntArrayList>();
		//traceIDtraceName = new IntObjectHashMap<String>();
		labelMapping = HashBiMap.create();
		inverseLabelMapping = HashBiMap.create();
		int translation = 1;
		int iTransition = 0;
		IntArrayList tr;// = new IntArrayList();
		IntDAFSAInt fsa = new IntDAFSAInt();
		//fsa.setFinalValue(1);
		//TIntArrayList traceList = new TIntArrayList();
		Integer key = null;
		Set<IntArrayList> visited = new HashSet<IntArrayList>();
		//Set<XTrace> uniqueTraces = new HashSet<XTrace>(xLog);
		int it = 1;
		
		for (XTrace trace : xLog)
		{
			tr = new IntArrayList();
			//traceList.resetQuick();
			for (XEvent event : trace)
			{
				String eventName = xce.extractName(event);
				if ((key = inverseLabelMapping.get(eventName)) == null)
				{
					inverseLabelMapping.put(eventName, translation);
					labelMapping.put(translation, eventName);
					key = translation;
					translation++;
				}
				//traceList.add(key);
				tr.add(key);
			}
			//TroveUtils.expand(traceList, tr.toArray());
//			List<String> traceIDs;
//			if((traceIDs = tracesContained.get(tr))==null)
//			{
//				traceIDs = new ArrayList<String>();
//				tracesContained.put(tr, traceIDs);
//			}
//			traceIDs.add(xce.extractName(trace));
//			if(!tracesContained.containsKey(tr))
//				tracesContained.put(tr, false);
			if(!caseTracesMapping.containsKey(tr))
				caseTracesMapping.put(tr, new IntArrayList());
			caseTracesMapping.get(tr).add(it);
			//traceIDtraceName.put(it, xce.extractName(trace));
			if(visited.add(tr))
				fsa.addMinWord(tr);//traceList);
			it++;
		}
		//long automaton = System.nanoTime();
		//fsa.toDot("DAFSA_Mapping_for_Log_" + ImportLog.logName.substring(0, ImportLog.logName.length()-4) + ".dot");
		
		int idest=0;
		int ilabel=0;
		int initialState = 0;
		stateMapping = HashBiMap.create();
		transitionMapping = HashBiMap.create();
		finalStates = new IntHashSet(); 
		for(AbstractIntDAFSA.State n : fsa.getStates())
		{	
			if(!(n.outbound()==0 && (!fsa.isFinalState(n.getNumber()))))
			{
				if(!stateMapping.containsKey(n.getNumber()))
					stateMapping.put(n.getNumber(), new State(n.getNumber(), fsa.isSource(n.getNumber()), fsa.isFinalState(n.getNumber())));
				if(initialState !=0 && fsa.isSource(n.getNumber())){initialState = n.getNumber();}
				if(fsa.isFinalState(n.getNumber())){finalStates.add(n.getNumber());}
				for(int i = 0; i < n.outbound(); i++) 
				{
					idest = AbstractIntDAFSA.decodeDest(n.next.get(i));
					ilabel = AbstractIntDAFSA.decodeLabel(n.next.get(i));
				
					if (!stateMapping.containsKey(idest))
						stateMapping.put(idest, new State(idest, fsa.isSource(idest), fsa.isFinalState(AbstractIntDAFSA.decodeDest(n.next.get(i)))));
					iTransition++;
					Transition t = new Transition(stateMapping.get(n.getNumber()), stateMapping.get(idest), ilabel);
					transitionMapping.put(iTransition, t);
					stateMapping.get(n.getNumber()).outgoingTransitions().add(t);
					stateMapping.get(idest).incomingTransitions().add(t);
				}
			}
		}
		
		//AlphaRelations concurrencyOracle = new AlphaRelations(xLog);
		/*
		for(String keyActivity : concurrencyOracle.getConcurrency().keySet())
			{
				System.out.println("Concurrent Activities: " + keyActivity + " || " + concurrencyOracle.getConcurrency().get(keyActivity));;
			}
		*/
		Automaton logAutomaton = new Automaton(stateMapping, labelMapping, inverseLabelMapping, transitionMapping, initialState, finalStates, caseTracesMapping);//, concurrencyOracle);
		//long conversion = System.nanoTime();
		//System.out.println("Log Automaton creation: " + TimeUnit.MILLISECONDS.convert((automaton - start), TimeUnit.NANOSECONDS) + "ms");
		//System.out.println("Log Automaton conversion: " + TimeUnit.MILLISECONDS.convert((conversion - automaton), TimeUnit.NANOSECONDS) + "ms");
		
		/*
		Set<Set<Integer>> finalConfigurations = new LinkedHashSet<Set<Integer>>(Configurations);
		for(Set<Integer> finalConfiguration : finalConfigurations)
		{
			String config = "Final Configuration: {";
			for(int event : finalConfiguration)
			{
				config = config + labelMapping.inverse().get(event) + ", "; 
			}
			boolean contained = false;
			for(Set<Set<Integer>> automatonFinalConfigurations : logAutomaton.finalConfigurations().values())
				for(Set<Integer> automatonFinalConfiguration : automatonFinalConfigurations)
					contained = contained || automatonFinalConfiguration.containsAll(finalConfiguration);
			config=config.substring(0, config.length() - 2) +"}, contained: " + contained;
			System.out.println(config);
		}
		*/
		return logAutomaton; //logAutomaton;
	}
	
	public Map<String, Integer> getlabelMapping()
	{
		return inverseLabelMapping;
	}
}
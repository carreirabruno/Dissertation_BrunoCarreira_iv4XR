# Collaboration analysis in the iv4XR framework


This is a copy of the [iv4xr Agent-based Testing Framework Demo](https://github.com/iv4xr-project/iv4xrDemo), used and modified for the dissertation of the Instituto Superior Técnico, Universidade de Lisboa student Bruno Carreira.

This repository contains all the original files from the copied version of the iv4XR framework, for any information regarding instalation and other related matters, please check the https://github.com/iv4xr-project/iv4xrDemo. All the added files and funtions were done using the the prefix "Bruno_" representating the student implenting the work.

**What is this dissertation objective** This work aims at helping developers of interactive software to test collaboration inducing scenarios. When creating a training simulation for team building, developers must make sure that their scenarios promote collaboration but also, don't force it, meaning a scenario must allow users to behave freely, otherwise did they really collaborated or were just forced to? This creates a difficulty, how can developers test their scenarios on their capability of allowing different behaviours? Our approach is based on using two different automated agents behavioural traces, one specifying the scenario's Design Goal, collaboration, and the other an example of a non-Design Goal, acting individually. After training said agents and by comparing the agents optimal behaviour when solving each scenario to the two policies, we can determine if the scenarios allow to differentiate between the Design Goal and the non-Design Goal. With this approach we are also able to order the scenarios from easiest to differentiate to hardest.

**What is in this repository?** This repositorty contains the files used by the student to create the two types of automated agents and learn their policies, Centralized and Individual. 
It also contains the files used to perform a comparison between the agents optimal behavioural trace and both policies as well as the classes for comparing the same policies to real-life users behaviuoural traces in order to corroborate our work.

**Work in progress notice.** please keep in mind that the work in this repository is still in progress, meaning the code/files can look chaotic. Updates and changes will be made in the future.

**Student:**
Bruno Carreira.

**Supervisors:**
Rui Prada,
Manuel Lopes.

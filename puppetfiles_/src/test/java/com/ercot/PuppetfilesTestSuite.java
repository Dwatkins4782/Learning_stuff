package com.ercot;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.ercot.puppetfiles.v1.controller.PuppetfilesControllerTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
  PuppetfilesControllerTest.class
})
public class PuppetfilesTestSuite {}
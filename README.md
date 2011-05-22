lessStyle provided two modes for ZK developers
1.Instant Mode (Default) - for development and debugging environment
LESS files (.less) will be compiled to standard css files (.less.css) in every request. We also provided an attribute name "recompile" to disable the compile operation.
    <lessStyle src="test1.less" />
    <!-- Load /WebContent/test.less.css -->
    
    <lessStyle>
    @basic-width : 30px;
    .div { 
      .div-sub {
         width: @basic-width + 30;
      }
      width: @basic-width;
    }
    </lessStyle>
    <!-- Load compiled css block -->

2.Static Mode - for deployment environment
LESS files will be compiled to standard css files only when starting-up of the web application. lessStyle component will load css file src from serviceURI where LessService servlet mapped.

    <lessStyle mode="static" src="test.less" />
    <!-- Load /WebContent/WEB-INF/lessSrc/test.less.css through LessService - /less/test.less.css-->

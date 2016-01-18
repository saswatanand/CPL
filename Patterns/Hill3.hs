module Hill3 where
--default import start--
import IOExts
import CPLTI
import CPLPattern
import Prelude hiding ((>),(==),(<),(<=),(>=),(&&),(||))
import BuiltinPatterns
--default import end--

hill3 :: Pattern
hill3 = (hill >.> hill >.> hill) ?? (\[a,b,c,d,e,f,g]->
	     let e0_ = (close .# 1) a
		 e0 = close a
		 e1 = close b
		 e2 = close c
		 e3 = close d
		 e4 = close e
	  	 e5 = close f
		 e6 = close g
		 e6_ = (close .# 1) g
		 x = (e1 + e5) 
 	     in [e1 > e0 && e1 >e2,
		 e3 > e1 && e3 > e5,
		 e5 > e4 && e5 > 46])
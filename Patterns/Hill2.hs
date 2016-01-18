module Hill2 where
--default import start--

import Prelude hiding ((>),(==),(<),(<=),(>=),(&&),(||))
import BuiltinPatterns
import CPLTI
import CPLPattern
--default import end--

hill2 :: Pattern 
hill2 = hill >.> hill
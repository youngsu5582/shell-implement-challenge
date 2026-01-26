## Backslash within double quotes

이전 상태의 저장이 필요해졌다.  

`echo "A \" inside double quotes"`  

와 같을때, \" 을 처리하고 다시 `DOUBLE_QUOTED` 상태로 돌아가져야 한다.  
상태를 두개로 관리..?  